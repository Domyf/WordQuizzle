package com.domenico.client;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.TCPConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class TCPClient implements TCPListener {

    private final TCPNClient tcpnClient;
    private final Object mutex = new Object();
    private ConnectionData received = null;

    public TCPClient() throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(TCPConnection.SERVER_HOST, TCPConnection.SERVER_PORT);
        this.tcpnClient = new TCPNClient(SocketChannel.open(socketAddress), this);
        new Thread(tcpnClient).start();
    }

    @Override
    public void onTCPDataReceived(ConnectionData received) {
        if (ConnectionData.Validator.isChallengeEnd(received)) {
            System.out.println("Sfida terminata");
        } else {
            synchronized (mutex) {
                this.received = received;
                mutex.notify();
            }
        }
    }

    private ConnectionData waitResponseFromServer() throws InterruptedException {
        ConnectionData response;
        synchronized (mutex) {
            while (this.received == null) {
                mutex.wait();
            }
            response = this.received;
            this.received = null;
        }
        return response;
    }

    public String login(String username, String password, int udpPort) throws InterruptedException {
        ConnectionData request = ConnectionData.Factory.newLoginRequest(username, password, udpPort);
        tcpnClient.sendToServer(request);
        ConnectionData response = waitResponseFromServer();
        if (ConnectionData.Validator.isSuccessResponse(response)) {
            return "";
        } else if (ConnectionData.Validator.isFailResponse(response))
            return response.getResponseData();
        return null;
    }

    public String logout(String username) throws IOException, InterruptedException {
        ConnectionData request = ConnectionData.Factory.newLogoutRequest(username);
        tcpnClient.sendToServer(request);
        ConnectionData response = waitResponseFromServer();
        if (ConnectionData.Validator.isSuccessResponse(response))
            return "";
        else if (ConnectionData.Validator.isFailResponse(response))
            return response.getResponseData();
        return null;
    }

    public String addFriend(String username, String friendUsername) throws IOException, InterruptedException {
        ConnectionData request = ConnectionData.Factory.newAddFriendRequest(username, friendUsername);
        tcpnClient.sendToServer(request);
        ConnectionData response = waitResponseFromServer();
        if (ConnectionData.Validator.isSuccessResponse(response))
            return "Tu e "+friendUsername+" siete ora amici!";
        else if (ConnectionData.Validator.isFailResponse(response))
            return response.getResponseData();
        return null;
    }

    public JSONArray friendList(String username) throws IOException, InterruptedException {
        ConnectionData request = ConnectionData.Factory.newFriendListRequest(username);
        tcpnClient.sendToServer(request);
        ConnectionData response = waitResponseFromServer();
        String jsonString = response.getResponseData();

        return (JSONArray) JSONValue.parse(jsonString);
    }

    public String sendChallengeRequest(String username, String friendUsername) throws IOException, InterruptedException {
        ConnectionData request = ConnectionData.Factory.newChallengeRequest(username, friendUsername);
        tcpnClient.sendToServer(request);
        ConnectionData response = waitResponseFromServer();
        if (ConnectionData.Validator.isSuccessResponse(response)) {   //The challenge has been forwarded
            return "";
        } else if (ConnectionData.Validator.isFailResponse(response)) { //The challenge cannot be forwarded
            return response.getResponseData();
        }
        System.out.println("error");    //TODO remove this
        return null;    //error occurred
    }

    public boolean getChallengeResponse(StringBuffer response) throws IOException, InterruptedException {
        ConnectionData data = waitResponseFromServer();
        response.append(data.getResponseData());

        return ConnectionData.Validator.isSuccessResponse(data);
    }

    public String getNextWord() throws IOException, InterruptedException {
        ConnectionData data = waitResponseFromServer();
        if (ConnectionData.Validator.isChallengeWord(data))
            return data.getResponseData();
        return null;
    }

    public String showScore(String username) throws IOException, InterruptedException {
        ConnectionData request = ConnectionData.Factory.newScoreRequest(username);
        tcpnClient.sendToServer(request);
        ConnectionData response = waitResponseFromServer();
        return response.getResponseData();
    }

    public JSONObject showLeaderboard(String username) throws IOException, InterruptedException {
        ConnectionData request = ConnectionData.Factory.newLeaderboardRequest(username);
        tcpnClient.sendToServer(request);
        ConnectionData response = waitResponseFromServer();
        return (JSONObject) JSONValue.parse(response.getResponseData());
    }

    public String onChallengeStart(String[] challengeSettings) throws IOException, InterruptedException {
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

    public void exit() {
        tcpnClient.stopProcessing();
    }
}
