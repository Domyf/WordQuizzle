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
public class WQServer implements WQHandler {

    private final UsersManagement usersManagement = UsersManagement.getInstance();
    private final List<String> italianWords;            //list of italian words
    private final ExecutorService executors;            //executors that will run the ChallengeRequests. One of them also runs the UDP server
    private final TCPServer tcpServer;                  //thread that handles all the tcp communications
    private final UDPServer udpServer;                  //thread that handles all the udp communications
    private final Map<String, SelectionKey> mapToKey;   //maps username -> client's tcp key

    public WQServer(List<String> italianWords) throws IOException {
        this.executors = Executors.newCachedThreadPool();
        this.udpServer = new UDPServer();
        this.tcpServer = new TCPServer(this);
        this.mapToKey = new HashMap<>();
        this.italianWords = italianWords;
    }

    public void start() {
        executors.execute(udpServer);
        tcpServer.startProcessing();
        udpServer.stopProcessing();
    }

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
    public void handleChallengeWordsReady(Challenge challenge, SelectionKey fromKey, SelectionKey toKey) {
        //TODO handle the readiness of the challenge's words and send the first word to both
        TCPServer.Attachment fromUser = (TCPServer.Attachment) fromKey.attachment();
        TCPServer.Attachment toUser = (TCPServer.Attachment) toKey.attachment();
        String nextItWordFrom = challenge.getNextItWord(fromUser.username);
        String nextItWordTo = challenge.getNextItWord(toUser.username);
        //Sends the first word via tcp to both
        if (nextItWordFrom != null && nextItWordTo != null) {
            tcpServer.sendToClient(ConnectionData.Factory.newChallengeStart(Settings.MAX_CHALLENGE_LENGTH, Settings.CHALLENGE_WORDS, nextItWordFrom), fromKey);
            tcpServer.sendToClient(ConnectionData.Factory.newChallengeStart(Settings.MAX_CHALLENGE_LENGTH, Settings.CHALLENGE_WORDS, nextItWordTo), toKey);
            TimeIsUp.schedule(this::handleChallengeTimeout, Settings.MAX_CHALLENGE_LENGTH, fromKey, toKey);
        } else {
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

    private void handleChallengeTimeout(SelectionKey ...users) {
        TCPServer.Attachment fromUser = (TCPServer.Attachment) users[0].attachment();
        TCPServer.Attachment toUser = (TCPServer.Attachment) users[1].attachment();
        System.out.printf("Challenge ended (%s vs %s)\n", fromUser.username, toUser.username);
        if (fromUser.challenge != null) {
            fromUser.challenge.onChallengeEnded();
            toUser.challenge = null;
            fromUser.challenge = null;
            tcpServer.sendToClient(ConnectionData.Factory.newChallengeEnd(), users[0]);
            tcpServer.sendToClient(ConnectionData.Factory.newChallengeEnd(), users[1]);
        }
        //TODO shouldn't it be synchronized?!
    }
}