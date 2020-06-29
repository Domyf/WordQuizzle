package com.domenico.client;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.TCPConnection;
import com.domenico.shared.Utils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class TCPClient {

    private SocketChannel channel;
    private TCPConnection tcpConnection;

    public TCPClient() throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(TCPConnection.SERVER_HOST, TCPConnection.SERVER_PORT);
        channel = SocketChannel.open(socketAddress);
        tcpConnection = new TCPConnection(channel);
    }

    public boolean login(String username, String password, int udpPort) throws IOException {
        ConnectionData request = ConnectionData.Factory.newLoginRequest(username, password, udpPort);
        tcpConnection.sendData(request);
        ConnectionData response = tcpConnection.receiveData();
        if (ConnectionData.Validator.isSuccessResponse(response)) {
            System.out.println(Messages.LOGIN_SUCCESS);
            return true;
        }

        if (ConnectionData.Validator.isFailResponse(response))
            System.out.println(response.getResponseData());
        return false;
    }

    public void logout(String username) throws IOException {
        ConnectionData request = ConnectionData.Factory.newLogoutRequest(username);
        tcpConnection.sendData(request);
        ConnectionData response = tcpConnection.receiveData();
        if (ConnectionData.Validator.isSuccessResponse(response))
            System.out.println(Messages.LOGOUT_SUCCESS);
        else if (ConnectionData.Validator.isFailResponse(response))
            System.out.println(response.getResponseData());
    }

    public void addFriend(String username, String friendUsername) throws IOException {
        ConnectionData request = ConnectionData.Factory.newAddFriendRequest(username, friendUsername);
        tcpConnection.sendData(request);
        ConnectionData response = tcpConnection.receiveData();
        if (ConnectionData.Validator.isSuccessResponse(response))
            System.out.println("Tu e "+friendUsername+" siete ora amici!");
        else if (ConnectionData.Validator.isFailResponse(response))
            System.out.println(response.getResponseData());
    }

    public void friendList(String username) throws IOException {
        ConnectionData request = ConnectionData.Factory.newFriendListRequest(username);
        tcpConnection.sendData(request);
        ConnectionData response = tcpConnection.receiveData();
        String jsonString = response.getResponseData();
        try {
            JSONParser parser = new JSONParser();
            JSONArray jsonArray = (JSONArray) parser.parse(jsonString);
            if (jsonArray.isEmpty()) {
                System.out.println("Non hai nessun amico");
            } else {
                System.out.print("I tuoi amici sono: ");
                String friendList = Utils.stringify(jsonArray, ", ");
                System.out.println(friendList);
            }
        } catch (ParseException e) {
            System.out.println("C'è stato un problema, riprova più tardi");
        }
    }

    public void challenge(String username, String friendUsername) throws IOException {
        ConnectionData request = ConnectionData.Factory.newChallengeRequest(username, friendUsername);
        tcpConnection.sendData(request);
        System.out.println("Sfida inviata a "+friendUsername+". In attesa di risposta...");
        ConnectionData response = tcpConnection.receiveData();
        if (ConnectionData.Validator.isSuccessResponse(response))
            System.out.println(friendUsername+" ha accettato la sfida!");
        else if (ConnectionData.Validator.isFailResponse(response))
            System.out.println(response.getResponseData());
    }

    public void showScore(String username) throws IOException {
        ConnectionData request = ConnectionData.Factory.newScoreRequest(username);
        tcpConnection.sendData(request);
        ConnectionData response = tcpConnection.receiveData();
        System.out.println("Il tuo punteggio è: "+response.getResponseData());
    }

    public void showLeaderboard(String username) throws IOException {
        ConnectionData request = ConnectionData.Factory.newLeaderboardRequest(username);
        tcpConnection.sendData(request);
        ConnectionData response = tcpConnection.receiveData();
        System.out.println(response.getResponseData());
    }

    public void exit() throws IOException {
        tcpConnection.endConnection();
    }
}
