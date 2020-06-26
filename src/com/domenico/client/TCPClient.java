package com.domenico.client;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.TCPConnection;

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

    public boolean login(String username, String password) throws IOException {
        ConnectionData request = ConnectionData.Factory.newLoginRequest(username, password);
        tcpConnection.sendData(request);
        ConnectionData response = tcpConnection.receiveData();
        if (ConnectionData.Validator.isSuccessResponse(response)) {
            System.out.println("Login eseguito con successo");
            return true;
        }

        if (ConnectionData.Validator.isFailResponse(response))
            System.out.println(response.getMessage());
        return false;
    }

    public void logout(String username) throws IOException {
        ConnectionData request = ConnectionData.Factory.newLogoutRequest(username);
        tcpConnection.sendData(request);
        ConnectionData response = tcpConnection.receiveData();
        if (ConnectionData.Validator.isSuccessResponse(response))
            System.out.println("Logout eseguito con successo");
        else if (ConnectionData.Validator.isFailResponse(response))
            System.out.println(response.getMessage());
    }

    public void addFriend(String username, String friendUsername) throws IOException {
        ConnectionData request = ConnectionData.Factory.newAddFriendRequest(username, friendUsername);
        tcpConnection.sendData(request);
        ConnectionData response = tcpConnection.receiveData();
        if (ConnectionData.Validator.isSuccessResponse(response))
            System.out.println("Tu e "+friendUsername+" siete ora amici!");
        else if (ConnectionData.Validator.isFailResponse(response))
            System.out.println(response.getMessage());
    }

    public void friendList(String username) throws IOException {
        ConnectionData request = ConnectionData.Factory.newFriendListRequest(username);
        tcpConnection.sendData(request);
        ConnectionData response = tcpConnection.receiveData();
        System.out.println("Ritornato: "+response.toString());
    }

    public void showScore(String username) throws IOException {
        ConnectionData request = ConnectionData.Factory.newScoreRequest(username);
        tcpConnection.sendData(request);
        ConnectionData response = tcpConnection.receiveData();
        System.out.println("Ritornato: "+response.toString());
    }

    public void showLeaderboard(String username) throws IOException {
        ConnectionData request = ConnectionData.Factory.newLeaderboardRequest(username);
        tcpConnection.sendData(request);
        ConnectionData response = tcpConnection.receiveData();
        System.out.println("Ritornato: "+response.toString());
    }

    public void exit() throws IOException {
        tcpConnection.endConnection();
    }
}
