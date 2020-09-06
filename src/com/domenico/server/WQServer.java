package com.domenico.server;

import com.domenico.communication.*;
import com.domenico.server.network.ChallengeRequest;
import com.domenico.server.network.TCPServer;
import com.domenico.server.network.UDPServer;
import com.domenico.server.network.UserAttachment;
import com.domenico.server.usersmanagement.UsersManagement;
import com.domenico.server.usersmanagement.UsersManagementException;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.*;
import java.nio.channels.SelectionKey;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class implements the entire WordQuizzle server. It runs the TCP server, the UDP server and the RMI server.
 * It also implements a Word Quizzle handler which means that it can handle all the WordQuizzle requests that can arrive
 * from the clients.
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
        UserAttachment attachment = (UserAttachment) key.attachment();
        String username = connectionData.getUsername();
        String password = connectionData.getPassword();
        usersManagement.login(username, password);
        int udpPort = Integer.parseUnsignedInt(connectionData.getResponseData());
        attachment.setAddress(inetAddress, udpPort);
        attachment.setUsername(username);
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

        UserAttachment fromUser = (UserAttachment) key.attachment();
        SelectionKey toKey = mapToKey.get(to);
        UserAttachment toUser = (UserAttachment) toKey.attachment();
        //Il the user has already sent a challenge which is not timedout yet or it has not been accepted yet
        if (toUser.getChallenge() != null)
            throw new UsersManagementException(to+" ha una sfida in questo momento");
        if (fromUser.getChallenge() != null)
            throw new UsersManagementException("Non puoi avviare più sfide contemporaneamente");

        Challenge challenge = new Challenge(from, to);
        fromUser.setChallenge(challenge);
        toUser.setChallenge(challenge);
        //Handling the challenge request via udp
        executors.execute(new ChallengeRequest(this, udpServer, key, toKey, italianWords));

        //success because the challenge will be forwarded
        return ConnectionData.Factory.newSuccessResponse(); //Response is already available
    }

    //Async call
    @Override
    public void handleChallengeResponse(Challenge challenge, SelectionKey fromKey, SelectionKey toKey) {
        UserAttachment fromUser = (UserAttachment) fromKey.attachment();
        UserAttachment toUser = (UserAttachment) toKey.attachment();
        ConnectionData response;
        if (challenge.isRequestTimedOut()) {
            response = ConnectionData.Factory.newFailResponse("Tempo scaduto");
            //notify via udp who was challenged
            udpServer.challengeTimedout(challenge, toUser.getUdpAddress());
        } else if (challenge.isRequestAccepted()) {
            response = ConnectionData.Factory.newSuccessResponse(challenge.getTo() + " ha accettato la sfida");
        } else {
            response = ConnectionData.Factory.newFailResponse(challenge.getTo() + " ha rifiutato la sfida");
        }
        //notify via tcp who has sent the challenge
        tcpServer.sendToClient(response, fromKey);
        //Remove the challenge if it has not been accepted or it has timedout
        if (!challenge.isRequestAccepted()) {
            toUser.setChallenge(null);
            fromUser.setChallenge(null);
        }
    }

    @Override
    public void handleChallengeWordsReady(Challenge challenge, SelectionKey fromKey, SelectionKey toKey) {
        UserAttachment fromUser = (UserAttachment) fromKey.attachment();
        UserAttachment toUser = (UserAttachment) toKey.attachment();
        String nextItWordFrom = challenge.getNextItWord(fromUser.getUsername());
        String nextItWordTo = challenge.getNextItWord(toUser.getUsername());
        //Sends the first word via tcp to both
        if (nextItWordFrom != null && nextItWordTo != null) {
            long maxChallengeLength = Settings.getMaxChallengeLength();
            int challengeWords = Settings.getChallengeWords();
            tcpServer.sendToClient(
                    ConnectionData.Factory.newChallengeStart(maxChallengeLength, challengeWords, nextItWordFrom),
                    fromKey);
            tcpServer.sendToClient(
                    ConnectionData.Factory.newChallengeStart(maxChallengeLength, challengeWords, nextItWordTo),
                    toKey);
            TimerTask timer = TimeIsUp.schedule(this::handleChallengeTimeout, maxChallengeLength, fromKey, toKey);
            fromUser.getChallenge().setTimer(timer);
        } else {
            toUser.setChallenge(null);
            fromUser.setChallenge(null);
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
        UserAttachment attachment = (UserAttachment) key.attachment();
        try {
            usersManagement.logout(attachment.getUsername());
            mapToKey.remove(attachment.getUsername());
        } catch (UsersManagementException ignored) { }
    }

    @Override
    public synchronized ConnectionData handleTranslationArrived(ConnectionData received, SelectionKey key) {
        UserAttachment thisAttch = (UserAttachment) key.attachment();
        Challenge challenge = thisAttch.getChallenge();
        challenge.checkAndGoNext(thisAttch.getUsername(), received.getResponseData());
        //If the challenge is ended for both
        if (challenge.isGameEnded()) {
            SelectionKey otherKey;
            if (challenge.getFrom().equals(thisAttch.getUsername()))
                otherKey = mapToKey.get(challenge.getTo());
            else
                otherKey = mapToKey.get(challenge.getFrom());
            challenge.cancelTimer();
            UserAttachment otherAttach = (UserAttachment) otherKey.attachment();
            handleChallengeEnd(thisAttch, otherAttach);

            //Sends to the other that the challenge ended
            tcpServer.sendToClient(getChallengeEndByUsername(otherAttach.getUsername(), challenge), otherKey);
            //Sends to this player that the challenge ended
            return getChallengeEndByUsername(thisAttch.getUsername(), challenge);
        } else if (!challenge.hasPlayerEnded(thisAttch.getUsername())) { //If this was not the last word then sends the next one
            String nextItWord = challenge.getNextItWord(thisAttch.getUsername());
            //Sends the next word
            return ConnectionData.Factory.newChallengeWord(nextItWord);
        }

        //The user or both users have ended the challenge. Do not send a reply to this user
        //because it has already got the challenge end message
        return null;
    }

    /** Handle the end of the challenged between the given two users */
    private void handleChallengeEnd(UserAttachment first, UserAttachment second) {
        System.out.printf("Challenge ended (%s vs %s)\n", first.getUsername(), second.getUsername());
        Challenge challenge = first.getChallenge(); // first.chellenge == second.challenge
        if (challenge != null && second.getChallenge() != null) {
            challenge.onChallengeEnded(); //first.challenge == second.challenge
            try {
                usersManagement.addScore(first.getUsername(), challenge.getFinalPoints(first.getUsername()));
                usersManagement.addScore(second.getUsername(), challenge.getFinalPoints(second.getUsername()));
            } catch (UsersManagementException ignored) {}
            first.setChallenge(null);
            second.setChallenge(null);
        }
    }

    /** Handle the challenge timeout. It does just small work because it is called by a timer.
     * @param users the users involved into a challenge timeout
     * */
    private synchronized void handleChallengeTimeout(SelectionKey ...users) {
        UserAttachment first = (UserAttachment) users[0].attachment();
        UserAttachment second = (UserAttachment) users[1].attachment();
        Challenge challenge = first.getChallenge(); //first.challenge == second.challenge
        handleChallengeEnd(first, second);
        tcpServer.sendToClient(getChallengeEndByUsername(first.getUsername(), challenge), users[0]);
        tcpServer.sendToClient(getChallengeEndByUsername(second.getUsername(), challenge), users[1]);
    }

    /** By giving the username and the chellenge, it returns a ConnectionData which contains the stats of
     * the ended challenge.
     * */
    private synchronized ConnectionData getChallengeEndByUsername(String username, Challenge challenge) {
        int correct = challenge.getRightCounter(username);
        int wrong = challenge.getWrongCounter(username);
        int notransl = Settings.getChallengeWords() - (correct + wrong);
        int yourscore = challenge.getPoints(username);
        int otherscore = challenge.getOtherPoints(username);
        int extrapoints = yourscore > otherscore ? Settings.getExtraPoints() : 0;

        return ConnectionData.Factory.newChallengeEnd(correct, wrong, notransl, yourscore, otherscore, extrapoints);
    }
}