package com.domenico.client;

import com.domenico.communication.Connection;
import com.domenico.communication.ConnectionData;
import com.domenico.communication.UDPConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

public class UDPClient implements Runnable {

    private DatagramChannel channel;
    private UDPConnection udpConnection;
    private String loggedUsername;

    public UDPClient() throws IOException {
        channel = DatagramChannel.open();
        channel.socket().bind(new InetSocketAddress(0));
        SocketAddress serverAddress = new InetSocketAddress(UDPConnection.HOST_NAME, UDPConnection.PORT);
        udpConnection = new UDPConnection(channel, serverAddress);
    }

    public void setLoggedUsername(String loggedUsername) {
        this.loggedUsername = loggedUsername;
    }

    public void startGame(String username, String friendUsername) throws IOException {
        /*udpConnection.sendData(ConnectionData.Factory.newChallengeRequest);
        ConnectionData response = udpConnection.receiveData();
        System.out.println("Ritornato: "+response.toString());*/
        System.out.println("To be implemented...");
    }

    public void exit() throws IOException {
        udpConnection.endConnection();
    }

    @Override
    public void run() {
        try {
            udpConnection.sendData(ConnectionData.Factory.newAddFriendRequest(loggedUsername, "placeholder"));
            ConnectionData receiveData = udpConnection.receiveData();
            if (ConnectionData.Validator.isSuccessResponse(receiveData))
                System.out.println("CONNESSO via UDP");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
