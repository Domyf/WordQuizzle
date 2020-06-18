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

    public void login(String username, String password) throws IOException {
        ConnectionData request = ConnectionData.Factory.newLoginRequest(username, password);
        tcpConnection.sendData(request);
        ConnectionData response = tcpConnection.receiveData();
        System.out.println("Ritornato: "+response.toString());
    }

    public void logout(String username) throws IOException {
        ConnectionData request = ConnectionData.Factory.newLogoutRequest(username);
        tcpConnection.sendData(request);
        ConnectionData response = tcpConnection.receiveData();
        System.out.println("Ritornato: "+response.toString());
    }

    public void addFriend(String username, String friendUsername) throws IOException {
        ConnectionData request = ConnectionData.Factory.newAddFriendRequest(username, friendUsername);
        tcpConnection.sendData(request);
        ConnectionData response = tcpConnection.receiveData();
        System.out.println("Ritornato: "+response.toString());
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
