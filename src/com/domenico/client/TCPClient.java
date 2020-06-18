package com.domenico.client;

import com.domenico.communication.LoginRequest;
import com.domenico.communication.Response;
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
        LoginRequest request = new LoginRequest(username, password);
        tcpConnection.sendRequest(request);
        Response response = tcpConnection.getResponse();
        System.out.println("Ritornato: "+response.toString());
    }

    public void logout(String username) throws IOException {
        //tcpConnection.sendRequest(request);
    }

    public void addFriend(String username, String friendUsername) throws IOException {
        //tcpConnection.sendRequest(request);
    }

    public void friendList(String username) throws IOException {
        //tcpConnection.sendRequest(request);
    }

    public void showScore(String username) throws IOException {
        //tcpConnection.sendRequest(request);
    }

    public void showLeaderboard(String username) throws IOException {
        //tcpConnection.sendRequest(request);
    }
}
