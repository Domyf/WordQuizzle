package com.domenico.client;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.TCPConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class TCPClient {

    private final TCPConnection tcpConnection;

    public TCPClient() throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(TCPConnection.SERVER_HOST, TCPConnection.SERVER_PORT);
        SocketChannel channel = SocketChannel.open(socketAddress);
        tcpConnection = new TCPConnection(channel);
    }

    private ConnectionData sendTCPRequest(ConnectionData request) throws IOException {
        tcpConnection.sendData(request);
        return tcpConnection.receiveData();
    }

    public String login(String username, String password, int udpPort) throws IOException {
        ConnectionData request = ConnectionData.Factory.newLoginRequest(username, password, udpPort);
        ConnectionData response = sendTCPRequest(request);
        if (ConnectionData.Validator.isSuccessResponse(response)) {
            return "";
        } else if (ConnectionData.Validator.isFailResponse(response))
            return response.getResponseData();
        return null;
    }

    public String logout(String username) throws IOException {
        ConnectionData request = ConnectionData.Factory.newLogoutRequest(username);
        ConnectionData response = sendTCPRequest(request);
        if (ConnectionData.Validator.isSuccessResponse(response))
            return "";
        else if (ConnectionData.Validator.isFailResponse(response))
            return response.getResponseData();
        return null;
    }

    public String addFriend(String username, String friendUsername) throws IOException {
        ConnectionData request = ConnectionData.Factory.newAddFriendRequest(username, friendUsername);
        ConnectionData response = sendTCPRequest(request);
        if (ConnectionData.Validator.isSuccessResponse(response))
            return "Tu e "+friendUsername+" siete ora amici!";
        else if (ConnectionData.Validator.isFailResponse(response))
            return response.getResponseData();
        return null;
    }

    public JSONArray friendList(String username) throws IOException {
        ConnectionData request = ConnectionData.Factory.newFriendListRequest(username);
        ConnectionData response = sendTCPRequest(request);
        String jsonString = response.getResponseData();
        try {
            JSONParser parser = new JSONParser();
            JSONArray jsonArray = (JSONArray) parser.parse(jsonString);
            return jsonArray;
        } catch (ParseException e) {
            //System.out.println("C'è stato un problema, riprova più tardi");
        }
        return null;
    }

    public String challengeRequest(String username, String friendUsername) throws IOException {
        ConnectionData request = ConnectionData.Factory.newChallengeRequest(username, friendUsername);
        ConnectionData response = sendTCPRequest(request);
        if (ConnectionData.Validator.isSuccessResponse(response)) {   //The challenge has been forwarded
            return "";
        } else if (ConnectionData.Validator.isFailResponse(response)) { //The challenge cannot be forwarded
            return response.getResponseData();
        }
        return null;
    }

    public boolean challengeResponse() throws IOException {
        ConnectionData response = tcpConnection.receiveData();
        if (ConnectionData.Validator.isSuccessResponse(response))
            return true;
        else if (ConnectionData.Validator.isFailResponse(response))
            return false;
        return false;
    }

    public String showScore(String username) throws IOException {
        ConnectionData request = ConnectionData.Factory.newScoreRequest(username);
        ConnectionData response = sendTCPRequest(request);
        return response.getResponseData();
    }

    public JSONObject showLeaderboard(String username) throws IOException {
        ConnectionData request = ConnectionData.Factory.newLeaderboardRequest(username);
        ConnectionData response = sendTCPRequest(request);
        return (JSONObject) JSONValue.parse(response.getResponseData());
    }

    public void exit() throws IOException {
        tcpConnection.endConnection();
    }
}
