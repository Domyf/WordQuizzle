package com.domenico.server;

import com.domenico.communication.*;
import com.domenico.shared.Multiplexer;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.*;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is a worker class that manages all the TCP communications. It implements channel multiplexing to efficiently
 * manage all the clients by extending the Multiplexer class.
 */
public class WQServer extends Multiplexer implements TimeIsUpListener {

    private final UsersManagement usersManagement;
    private final UDPServer udpServer;
    private final Map<String, SelectionKey> mapToKey;   //maps username -> client's key
    private final Object timeoutmutex = new Object();   //mutex to handle concurrently the timedout challanges
    private final List<String> timedoutusers;           //users which are in a timedout challenge
    private final ExecutorService executors;            //executors that will run the ChallengeRequests. One of them also runs the UDP server
    private final List<String> italianWords;

    private static class UserRecord {
        String username;
        TCPConnection tcpConnection;
        ConnectionData response;
        int udpPort;
        InetAddress address;
        Challenge challenge;
    }

    public WQServer(List<String> italianWords) throws IOException {
        super(ServerSocketChannel.open(), SelectionKey.OP_ACCEPT);
        ServerSocket serverSocket = ((ServerSocketChannel) channel).socket();
        serverSocket.bind(new InetSocketAddress(TCPConnection.SERVER_PORT));
        print("Listening on port " + TCPConnection.SERVER_PORT);

        this.usersManagement = UsersManagement.getInstance();
        this.executors = Executors.newCachedThreadPool();
        this.udpServer = new UDPServer(DatagramChannel.open());
        this.executors.execute(this.udpServer);
        this.mapToKey = new HashMap<>();
        this.timedoutusers = new ArrayList<>();
        this.italianWords = italianWords;
    }

    /** Called when the method accept() will not block the thread */
    @Override
    protected void onAcceptable(SelectionKey key) throws IOException {
        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
        SocketChannel client = channel.accept();
        print("Accepted connection for "+client.getRemoteAddress());
        client.configureBlocking(false);    //non-blocking

        UserRecord attachment = new UserRecord();
        attachment.tcpConnection = new TCPConnection(client);
        attachment.address = client.socket().getInetAddress();
        attachment.response = null;
        attachment.challenge = null;

        client.register(selector, SelectionKey.OP_READ, attachment);
    }

    /**
     * Called when the method read() will not block the thread
     */
    @Override
    protected void onReadable(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        UserRecord attachment = (UserRecord) key.attachment();
        TCPConnection tcpConnection = attachment.tcpConnection;

        attachment.response = null;
        ConnectionData received = tcpConnection.receiveData();
        try {
            if (ConnectionData.Validator.isLoginRequest(received)) {
                attachment.response = handleLoginRequest(received, attachment, key);

            } else if (ConnectionData.Validator.isLogoutRequest(received)) {
                attachment.response = handleLogoutRequest(received, attachment);

            } else if (ConnectionData.Validator.isAddFriendRequest(received)) {
                attachment.response = handleAddFriendRequest(received);

            } else if (ConnectionData.Validator.isFriendListRequest(received)) {
                attachment.response = handleFriendListRequest(received);

            } else if (ConnectionData.Validator.isChallengeRequest(received)) {
                attachment.response = handleChallengeRequest(received, attachment);

            } else if (ConnectionData.Validator.isScoreRequest(received)) {
                attachment.response = handleScoreRequest(received);

            } else if (ConnectionData.Validator.isLeaderboardRequest(received)) {
                attachment.response = handleLeaderboardRequest(received);

            }
        } catch (UsersManagementException e) {
            attachment.response = ConnectionData.Factory.newFailResponse(e.getMessage());
        }
        print(received.toString(), "<-", client.getRemoteAddress(), attachment.username);
        client.register(selector, SelectionKey.OP_WRITE, attachment);
    }

    private ConnectionData handleLoginRequest(ConnectionData connectionData, UserRecord userRecord, SelectionKey key) throws UsersManagementException {
        String username = connectionData.getUsername();
        String password = connectionData.getPassword();
        usersManagement.login(username, password);
        userRecord.udpPort = Integer.parseUnsignedInt(connectionData.getResponseData());
        userRecord.username = username;
        mapToKey.put(username, key);
        return ConnectionData.Factory.newSuccessResponse();
    }

    private ConnectionData handleLogoutRequest(ConnectionData received, UserRecord attachment) throws UsersManagementException {
        usersManagement.logout(received.getUsername());
        mapToKey.remove(received.getUsername());
        //TODO is logging out while has a challenge
        if (attachment.challenge != null) {
            attachment.challenge.setAccepted(false);
        }

        return ConnectionData.Factory.newSuccessResponse();
    }

    private ConnectionData handleAddFriendRequest(ConnectionData received) throws UsersManagementException {
        String username = received.getUsername();
        String friend = received.getFriendUsername();
        usersManagement.addFriend(username, friend);

        return ConnectionData.Factory.newSuccessResponse();
    }

    private ConnectionData handleFriendListRequest(ConnectionData received) throws UsersManagementException {
        String jsonFriendList = usersManagement.getJSONFriendList(received.getUsername());

        return ConnectionData.Factory.newSuccessResponse(jsonFriendList);
    }

    private ConnectionData handleChallengeRequest(ConnectionData received, UserRecord fromUserRecord) throws IOException, UsersManagementException {
        //Throws an exception if the request is not valid and the error is sent back to who requested the challenge
        String from = received.getUsername();
        String to = received.getFriendUsername();
        print("Challenge arrived: "+from+" wants to challenge "+to);

        if (from.equals(to))
            throw new UsersManagementException("Non puoi sfidare te stesso");
        if (!usersManagement.areFriends(from, to))
            throw new UsersManagementException("Tu e "+to+" non siete amici");
        if (!usersManagement.isOnline(to))
            throw new UsersManagementException(to+" non è online in questo momento");

        SelectionKey toKey = mapToKey.get(to);
        UserRecord toRecord = (UserRecord) toKey.attachment();
        //Il the user has already sent a challenge which is not timedout yet or it has not been accepted yet
        if (toRecord.challenge != null)
            throw new UsersManagementException(to+" ha una sfida in questo momento");
        if (fromUserRecord.challenge != null)
            throw new UsersManagementException("Non puoi avviare più sfide contemporaneamente");

        InetSocketAddress toUDPAddress = new InetSocketAddress(toRecord.address, toRecord.udpPort);
        Challenge challenge = new Challenge(from, to);
        fromUserRecord.challenge = challenge;
        toRecord.challenge = challenge;
        //Handling the challenge request via udp
        executors.execute(new ChallengeRequest(udpServer, toUDPAddress, challenge, italianWords));

        return ConnectionData.Factory.newSuccessResponse();
    }

    private ConnectionData handleChallengeResponse(Challenge challenge) {
        if (challenge.isTimedout())
            return ConnectionData.Factory.newFailResponse("Tempo scaduto");
        else if (challenge.isAccepted())
            return ConnectionData.Factory.newSuccessResponse(challenge.getTo()+" ha accettato la sfida");

        return ConnectionData.Factory.newFailResponse(challenge.getTo()+" ha rifiutato la sfida");
    }

    private ConnectionData handleScoreRequest(ConnectionData received) throws UsersManagementException {
        int score = usersManagement.getScore(received.getUsername());

        return ConnectionData.Factory.newSuccessResponse(""+score);
    }

    private ConnectionData handleLeaderboardRequest(ConnectionData received) throws UsersManagementException {
        String username = received.getUsername();
        JSONObject leaderboard = usersManagement.getLeaderboard(username);
        return ConnectionData.Factory.newSuccessResponse(leaderboard.toJSONString());
    }

    /**
     * Called when the method write() will not block the thread
     */
    @Override
    protected void onWritable(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        UserRecord attachment = (UserRecord) key.attachment();
        TCPConnection tcpConnection = attachment.tcpConnection;
        Challenge challenge = attachment.challenge;

        //Send the response if it is ready
        if (attachment.response != null) {
            print(attachment.response.toString(), "->", client.getRemoteAddress(), attachment.username);
            tcpConnection.sendData(attachment.response);
            attachment.response = null;
            if (challenge == null || challenge.playing) {    //if the user has not a challenge request or the game is started
                key.interestOps(SelectionKey.OP_READ);
            }
        } else if (challenge != null && challenge.hasResponseOrTimeout()) {
            ConnectionData response = null;
            //Sends the challenge response to who started the challenge request
            if (!challenge.isResponseSentBack() && challenge.isFromUser(attachment.username)) {
                challenge.setResponseSentBack(true);
                response = handleChallengeResponse(challenge);
                //Remove the challenge if it has not been accepted or it has timedout
                if (!challenge.isAccepted()) {
                    attachment.challenge = null;
                    key.interestOps(SelectionKey.OP_READ);
                    SelectionKey otherKey = mapToKey.get(challenge.getTo());
                    if (otherKey != null) {
                        ((UserRecord) otherKey.attachment()).challenge = null;
                    }
                }
            } else if (challenge.hasWords()) {
                attachment.challenge = null;    //TODO iniziare la partita anzichè finirla
                key.interestOps(SelectionKey.OP_READ);
                if (challenge.isFromUser(attachment.username)) {
                    SelectionKey otherKey = mapToKey.get(challenge.getTo());
                    if (otherKey != null) {
                        otherKey.interestOps(SelectionKey.OP_WRITE);
                    }
                }
                //TimeIsUp.schedule(this, 6000, challengeRequest.from, challengeRequest.to);
            }
            if (response != null) {
                print(response.toString(), "->", client.getRemoteAddress(), attachment.username);
                tcpConnection.sendData(response);
            }
        }
    }

    /**
     * Called when the connection with a client is closed
     */
    @Override
    protected void onEndConnection(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        UserRecord attachment = (UserRecord) key.attachment();

        try {
            usersManagement.logout(attachment.username);
            mapToKey.remove(attachment.username);
        } catch (UsersManagementException ignored) { }

        print("Ended connection with "+client.getRemoteAddress());
    }

    @Override
    public void timeIsUp(Object... params) {
        synchronized (timeoutmutex) {
            for (Object param : params) {
                timedoutusers.add((String) param);
            }
        }
        super.wakeUp();
    }

    @Override
    protected void onWakeUp() {
        synchronized (timeoutmutex) {
            for (String user : timedoutusers) {
                SelectionKey key = mapToKey.get(user);
                if (key == null) continue;  //se l'utente si è disconnesso
                //UserRecord attachment = (UserRecord) key.attachment();
                //TODO inviare attachment.response = ConnectionData.Factory.newEndGameResponse();
                key.interestOps(SelectionKey.OP_WRITE);
            }
            timedoutusers.clear();
        }
    }

    public void print(String received, String direction, SocketAddress toAddress, String username) {
        System.out.printf("[TCP]: %s %s %s (%s)\n", received, direction, toAddress, username);
    }

    public void print(String str) {
        System.out.println("[TCP]: "+str);
    }
    @Override
    protected void onTimeout() {}   //never invoked because the select() has no timeout in this thread

}
