package com.domenico.client;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.TCPConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeoutException;

/** This class implements the client's functionalities that should be done via TCP. It runs a separated thread that
 * does the TCP communication with the server while this class acts like an handler for all the WordQuizzle
 * functionalities that should be done via TCP. */
public class TCPClient {

    //the runnable that handles all the tcp communications
    private final TCPMultiplexer tcpMultiplexer;
    //the word quizzle client
    private final WQClient wqClient;
    //the message arrived. Protected by the mutex
    private final Object mutex = new Object();
    private ConnectionData received = null;

    public TCPClient(WQClient wqClient) throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(TCPConnection.SERVER_HOST, TCPConnection.SERVER_PORT);
        this.tcpMultiplexer = new TCPMultiplexer(SocketChannel.open(socketAddress), this::onTCPDataReceived);
        new Thread(tcpMultiplexer).start();
        this.wqClient = wqClient;
    }

    /**
     * Invoked when a message arrives from the server
     * @param received the message arrived from the server
     */
    public void onTCPDataReceived(ConnectionData received) {
        if (ConnectionData.Validator.isChallengeWord(received)) {
            wqClient.setNextWord(received.getResponseData());
        } else if (ConnectionData.Validator.isChallengeEnd(received)) {
            String[] splitted = received.splitResponseData();
            int correct = Integer.parseUnsignedInt(splitted[0]);
            int wrong = Integer.parseUnsignedInt(splitted[1]);
            int notransl = Integer.parseUnsignedInt(splitted[2]);
            int yourscore = Integer.parseInt(splitted[3]);
            int otherscore = Integer.parseInt(splitted[4]);
            int extrapoints = Integer.parseUnsignedInt(splitted[5]);
            wqClient.onChallengeEnd(correct, wrong, notransl, yourscore, otherscore, extrapoints);
        } else {
            synchronized (mutex) {
                this.received = received;
                mutex.notify();
            }
        }
    }

    /**
     * Wait until the server sends back a response and returns it
     * @return the server response
     * @throws TimeoutException if the server doesn't reply to the request
     */
    private ConnectionData waitResponseFromServer() throws TimeoutException {
        ConnectionData response;
        synchronized (mutex) {
            long now = System.currentTimeMillis();
            long end = now + WQClient.SERVER_RESPONSE_TIMEOUT;
            while (this.received == null && now < end) {
                try {
                    mutex.wait(end - now);
                } catch (InterruptedException e) {
                    throw new TimeoutException("Connection ended with the server");
                }
                now = System.currentTimeMillis();
            }
            if (this.received != null) {
                response = this.received;
                this.received = null;
                return response;
            } else {
                throw new TimeoutException("Connection timeout");
            }
        }
    }

    /**
     * Sends a login request to the server via TCP. Returns an error string if something went wrong, like the username
     * or password are invalid but returns null if the response from the server is invalid. Returns an empty string in
     * case of success.
     *
     * @param username user's username
     * @param password user's password
     * @param udpPort user's udp port on which the UDP communication can be done
     * @return an empty string in case of success, an error string if something went wrong (like the username or
     * password are invalid), null if the response from the server is invalid
     * @throws TimeoutException if the server doesn't reply to the request
     */
    public String login(String username, String password, int udpPort) throws TimeoutException {
        ConnectionData request = ConnectionData.Factory.newLoginRequest(username, password, udpPort);
        tcpMultiplexer.sendToServer(request);
        ConnectionData response = waitResponseFromServer();
        if (ConnectionData.Validator.isSuccessResponse(response)) {
            return "";
        } else if (ConnectionData.Validator.isFailResponse(response))
            return response.getResponseData();
        return null;
    }

    /**
     * Log out the user from the word quizzle game
     * @param username user's username
     * @return an empty string in case of success, an error string if something went wrong (like the username is
     * invalid), null if the response from the server is invalid
     * @throws TimeoutException if the server doesn't reply to the request
     */
    public String logout(String username) throws TimeoutException {
        ConnectionData request = ConnectionData.Factory.newLogoutRequest(username);
        tcpMultiplexer.sendToServer(request);
        ConnectionData response = waitResponseFromServer();
        if (ConnectionData.Validator.isSuccessResponse(response))
            return "";
        else if (ConnectionData.Validator.isFailResponse(response))
            return response.getResponseData();
        return null;
    }

    /**
     * Adds a new friend defined by the given username to the user specified.
     * @param username user's username
     * @param friendUsername user's friend username
     * @return an empty string in case of success, an error string if something went wrong (like the username is
     * invalid), null if the response from the server is invalid
     * @throws TimeoutException if the server doesn't reply to the request
     */
    public String addFriend(String username, String friendUsername) throws TimeoutException {
        ConnectionData request = ConnectionData.Factory.newAddFriendRequest(username, friendUsername);
        tcpMultiplexer.sendToServer(request);
        ConnectionData response = waitResponseFromServer();
        if (ConnectionData.Validator.isSuccessResponse(response))
            return "";
        else if (ConnectionData.Validator.isFailResponse(response))
            return response.getResponseData();
        return null;
    }

    /**
     * Get the user's friend list as a JSON array.
     * @param username user's username
     * @return a JSONArray that contains the usernames of all the user's friends or null in case of a bad response from
     * the server
     * @throws TimeoutException if the server doesn't reply to the request
     */
    public JSONArray friendList(String username) throws TimeoutException {
        ConnectionData request = ConnectionData.Factory.newFriendListRequest(username);
        tcpMultiplexer.sendToServer(request);
        ConnectionData response = waitResponseFromServer();
        if (ConnectionData.Validator.isSuccessResponse(response)) {
            String jsonString = response.getResponseData();
            return (JSONArray) JSONValue.parse(jsonString);
        }
        return null;
    }

    /**
     * Send a challenge request to the specified user's friend.
     * @param username user's username
     * @param friendUsername user's friend username
     * @return an empty string in case of success, an error string if something went wrong (like the usernames are
     * invalid), null if the response from the server is invalid
     * @throws TimeoutException if the server doesn't reply to the request
     */
    public String sendChallengeRequest(String username, String friendUsername) throws TimeoutException {
        ConnectionData request = ConnectionData.Factory.newChallengeRequest(username, friendUsername);
        tcpMultiplexer.sendToServer(request);
        ConnectionData response = waitResponseFromServer();
        if (ConnectionData.Validator.isSuccessResponse(response)) {   //The challenge has been forwarded
            return "";
        } else if (ConnectionData.Validator.isFailResponse(response)) { //The challenge cannot be forwarded
            return response.getResponseData();
        }
        return null;
    }

    /**
     * Wait until the challenge response arrives.
     * @param response object to which append the server's response
     * @return true if the challenge has been accepted, false otherwise
     * @throws TimeoutException if the server doesn't reply to the request
     */
    public boolean getChallengeResponse(StringBuffer response) throws TimeoutException {
        ConnectionData data = waitResponseFromServer();
        response.append(data.getResponseData());

        return ConnectionData.Validator.isSuccessResponse(data);
    }

    /**
     * Get the user's score.
     * @param username user's username
     * @return the score in case of success, -1 if something went wrong
     * @throws TimeoutException if the server doesn't reply to the request
     */
    public int showScore(String username) throws TimeoutException {
        ConnectionData request = ConnectionData.Factory.newScoreRequest(username);
        tcpMultiplexer.sendToServer(request);
        ConnectionData response = waitResponseFromServer();
        if (ConnectionData.Validator.isSuccessResponse(response)) {
            String scoreString = response.getResponseData();
            try {
                return Integer.parseUnsignedInt(scoreString);
            } catch (NumberFormatException exception) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Get the leaderboard based on the user and its friends.
     * @param username user's username
     * @return a JSONObject which represent the leaderboard or null in case of a bad response from
     * the server
     * @throws TimeoutException if the server doesn't reply to the request
     */
    public JSONObject showLeaderboard(String username) throws TimeoutException {
        ConnectionData request = ConnectionData.Factory.newLeaderboardRequest(username);
        tcpMultiplexer.sendToServer(request);
        ConnectionData response = waitResponseFromServer();
        if (ConnectionData.Validator.isSuccessResponse(response))
            return (JSONObject) JSONValue.parse(response.getResponseData());
        return null;
    }

    /**
     * Waits until the server sends the message with all the challenge's settings and the first word.
     * @param challengeSettings array which should be modified with the challenge settings
     * @return the first word of the challenge or an empty string in case of error
     * @throws TimeoutException if the server doesn't reply to the request
     */
    public String onChallengeStart(String[] challengeSettings) throws TimeoutException {
        if (challengeSettings.length != 2)
            return "";
        ConnectionData received = waitResponseFromServer();
        if (ConnectionData.Validator.isChallengeStart(received)) {
            String[] splitted = received.splitResponseData();
            challengeSettings[0] = splitted[0];
            challengeSettings[1] = splitted[1];
            return splitted[2];
        }
        return "";
    }

    /**
     * Sends the given translation to the server.
     * @param translation the english translation to send to the server
     */
    public void sendTranslation(String translation) {
        ConnectionData request = ConnectionData.Factory.newChallengeWord(translation);
        tcpMultiplexer.sendToServer(request);
    }

    /** Stops all the TCP communications and the thread involved on it */
    public void exit() {
        synchronized (mutex) {
            mutex.notifyAll();
        }
        tcpMultiplexer.stopProcessing();
    }
}
