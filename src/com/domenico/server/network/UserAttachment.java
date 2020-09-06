package com.domenico.server.network;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.TCPConnection;
import com.domenico.server.Challenge;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/** Attachment for each user that connects to the server */
public class UserAttachment {

    //Utility object that wraps all the work that should be done to send or receive a ConnectionData object
    private final TCPConnection tcpConnection;
    //User's username
    private String username;
    //The challenge that the user is playing or null if the user is not playing
    private Challenge challenge;
    //The user's address (with the udp port)
    private InetSocketAddress udpAddress;
    //The response that should be sent to this user or null in case the response shouldn't be sent
    private ConnectionData response;

    public UserAttachment(SocketChannel client) {
        this.response = null;
        this.tcpConnection = new TCPConnection(client);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public InetSocketAddress getUdpAddress() {
        return udpAddress;
    }

    public void setAddress(InetAddress address, int port) {
        this.udpAddress = new InetSocketAddress(address, port);
    }

    public ConnectionData getResponse() {
        return response;
    }

    public void setResponse(ConnectionData response) {
        this.response = response;
    }

    public TCPConnection getTcpConnection() {
        return tcpConnection;
    }
}
