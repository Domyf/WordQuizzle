package com.domenico.server;

import com.domenico.communication.*;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.*;
import java.nio.channels.SelectionKey;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is a worker class that manages all the TCP communications. It implements channel multiplexing to efficiently
 * manage all the clients by extending the Multiplexer class.
 */
public class WQServer implements WQHandler, TimeIsUpListener {

    private final UsersManagement usersManagement = UsersManagement.getInstance();
    private final List<String> italianWords;            //list of italian words
    private final ExecutorService executors;            //executors that will run the ChallengeRequests. One of them also runs the UDP server
    private final TCPServer tcpServer;                  //thread that handles all the tcp communications
    private final UDPServer udpServer;                  //thread that handles all the udp communications
    private final Map<String, SelectionKey> mapToKey;   //maps username -> client's tcp key
    private final Object timeoutMutex = new Object();   //mutex to handle concurrently the timed out challenges
    private final List<String> timedoutUsers;           //users which are in a timed out challenge

    public WQServer(List<String> italianWords) throws IOException {
        this.executors = Executors.newCachedThreadPool();
        this.udpServer = new UDPServer();
        this.tcpServer = new TCPServer(this);
        this.mapToKey = new HashMap<>();
        this.timedoutUsers = new ArrayList<>();
        this.italianWords = italianWords;
    }

    public void start() {
        executors.execute(udpServer);
        tcpServer.startProcessing();
        udpServer.stopProcessing();
    }

    /*private void handleUserConnected(SelectionKey key, InetAddress address) {
        UserRecord userRecord = new UserRecord();
        userRecord.address = address;
        userRecord.challenge = null;
        userRecord.username = null;
    }*/

    @Override
    public ConnectionData handleLoginRequest(ConnectionData connectionData, SelectionKey key, InetAddress inetAddress) throws UsersManagementException {
        TCPServer.Attachment attachment = (TCPServer.Attachment) key.attachment();
        String username = connectionData.getUsername();
        String password = connectionData.getPassword();
        usersManagement.login(username, password);
        int udpPort = Integer.parseUnsignedInt(connectionData.getResponseData());
        attachment.address = new InetSocketAddress(inetAddress, udpPort);
        attachment.username = username;
        mapToKey.put(username, key);
        return ConnectionData.Factory.newSuccessResponse(); //Response is already available
    }

    @Override
    public ConnectionData handleLogoutRequest(ConnectionData received) throws UsersManagementException {
        usersManagement.logout(received.getUsername());
        mapToKey.remove(received.getUsername());

        return ConnectionData.Factory.newSuccessResponse(); //Response is already available
    }

    @Override
    public ConnectionData handleAddFriendRequest(ConnectionData received) throws UsersManagementException {
        String username = received.getUsername();
        String friend = received.getFriendUsername();
        usersManagement.addFriend(username, friend);

        return ConnectionData.Factory.newSuccessResponse(); //Response is already available
    }

    @Override
    public ConnectionData handleFriendListRequest(ConnectionData received) throws UsersManagementException {
        String jsonFriendList = usersManagement.getJSONFriendList(received.getUsername());

        return ConnectionData.Factory.newSuccessResponse(jsonFriendList); //Response is already available
    }

    @Override
    public ConnectionData handleChallengeRequest(ConnectionData received, SelectionKey key) throws UsersManagementException {
        //Throws an exception if the request is not valid and the error is sent back to who requested the challenge
        String from = received.getUsername();
        String to = received.getFriendUsername();
        System.out.println("Challenge arrived: "+from+" wants to challenge "+to);

        if (from.equals(to))
            throw new UsersManagementException("Non puoi sfidare te stesso");
        if (!usersManagement.areFriends(from, to))
            throw new UsersManagementException("Tu e "+to+" non siete amici");
        if (!usersManagement.isOnline(to))
            throw new UsersManagementException(to+" non è online in questo momento");

        TCPServer.Attachment fromUser = (TCPServer.Attachment) key.attachment();
        SelectionKey toKey = mapToKey.get(to);
        TCPServer.Attachment toUser = (TCPServer.Attachment) toKey.attachment();
        //Il the user has already sent a challenge which is not timedout yet or it has not been accepted yet
        if (toUser.challenge != null)
            throw new UsersManagementException(to+" ha una sfida in questo momento");
        if (fromUser.challenge != null)
            throw new UsersManagementException("Non puoi avviare più sfide contemporaneamente");

        Challenge challenge = new Challenge(from, to);
        fromUser.challenge = challenge;
        toUser.challenge = challenge;
        //Handling the challenge request via udp
        executors.execute(new ChallengeRequest(this, udpServer, key, toKey, italianWords));

        //success because the challenge will be forwarded
        return ConnectionData.Factory.newSuccessResponse(); //Response is already available
    }

    //Async call
    @Override
    public void handleChallengeResponse(Challenge challenge, SelectionKey fromKey, SelectionKey toKey) {
        TCPServer.Attachment fromUser = (TCPServer.Attachment) fromKey.attachment();
        TCPServer.Attachment toUser = (TCPServer.Attachment) toKey.attachment();
        ConnectionData response;
        if (challenge.isTimedOut()) {
            response = ConnectionData.Factory.newFailResponse("Tempo scaduto");
            //notify via udp who was challenged
            udpServer.challengeTimedout(challenge, toUser.address);
        } else if (challenge.isAccepted()) {
            response = ConnectionData.Factory.newSuccessResponse(challenge.getTo() + " ha accettato la sfida");
        } else {
            response = ConnectionData.Factory.newFailResponse(challenge.getTo() + " ha rifiutato la sfida");
        }
        //notify via tcp who has sent the challenge
        tcpServer.sendToClient(response, fromKey);
        //Remove the challenge if it has not been accepted or it has timedout
        if (!challenge.isAccepted()) {
            toUser.challenge = null;
            fromUser.challenge = null;
        }
    }

    @Override
    public ConnectionData handleScoreRequest(ConnectionData received) throws UsersManagementException {
        int score = usersManagement.getScore(received.getUsername());

        return ConnectionData.Factory.newSuccessResponse(""+score); //Response is already available
    }

    @Override
    public ConnectionData handleLeaderboardRequest(ConnectionData received) throws UsersManagementException {
        String username = received.getUsername();
        JSONObject leaderboard = usersManagement.getLeaderboard(username);
        return ConnectionData.Factory.newSuccessResponse(leaderboard.toJSONString()); //Response is already available
    }

    @Override
    public void handleUserDisconnected(SelectionKey key) {
        TCPServer.Attachment attachment = (TCPServer.Attachment) key.attachment();
        try {
            usersManagement.logout(attachment.username);
            mapToKey.remove(attachment.username);
        } catch (UsersManagementException ignored) { }
    }

    @Override
    public void timeIsUp(Object... params) {
        synchronized (timeoutMutex) {
            for (Object param : params) {
                timedoutUsers.add((String) param);
            }
        }
    }
}

/** TODO remove the following comment block
 * Called when the method write() will not block the thread */
    /*@Override
    protected void onWritable(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        UserRecord attachment = (UserRecord) key.attachment();
        TCPConnection tcpConnection = attachment.tcpConnection;
        Challenge challenge = attachment.challenge;

        if (attachment.response != null) {
            print(attachment.response.toString(), "->", client.getRemoteAddress(), attachment.username);
            tcpConnection.sendData(attachment.response);
            attachment.response = null;
            key.interestOps(SelectionKey.OP_READ);
        }

        //Send the response if it is ready
        /*if (attachment.response != null) {
            print(attachment.response.toString(), "->", client.getRemoteAddress(), attachment.username);
            tcpConnection.sendData(attachment.response);
            attachment.response = null;
            if (challenge == null) {    //if the user has not a challenge request or the game is started
                key.interestOps(SelectionKey.OP_READ);
            }
        } else if (challenge != null && challenge.hasResponseOrTimeout()) {
            ConnectionData response = null;
            //Sends the challenge response to who started the challenge request
            if (!challenge.isResponseSentBack() && challenge.isFromUser(attachment.username)) {
                challenge.setResponseSentBack(true);
                response = handleChallengeResponse(attachment, key);
            } else if (challenge.hasWords()) {
                String nextItWord = challenge.getNextItWord(attachment.username);
                response = ConnectionData.Factory.newChallengeWord(nextItWord);

                attachment.challenge = null;
                key.interestOps(SelectionKey.OP_READ);
                if (challenge.isFromUser(attachment.username)) {
                    SelectionKey otherKey = mapToKey.get(challenge.getTo());
                    if (otherKey != null) {
                        otherKey.interestOps(SelectionKey.OP_WRITE);
                    }
                    //TimeIsUp.schedule(this, 6000, challengeRequest.from, challengeRequest.to);
                }

                /*attachment.challenge = null;
                key.interestOps(SelectionKey.OP_READ);
                if (challenge.isFromUser(attachment.username)) {
                    SelectionKey otherKey = mapToKey.get(challenge.getTo());
                    if (otherKey != null) {
                        otherKey.interestOps(SelectionKey.OP_WRITE);
                    }
                }*/
            /*}
            if (response != null) {
                print(response.toString(), "->", client.getRemoteAddress(), attachment.username);
                tcpConnection.sendData(response);
            }
        }
    }*/