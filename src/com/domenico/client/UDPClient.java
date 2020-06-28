package com.domenico.client;

import com.domenico.communication.Connection;
import com.domenico.communication.ConnectionData;
import com.domenico.communication.UDPConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

public class UDPClient {

    private DatagramChannel channel;
    private SocketAddress serverAddress;
    private UDPConnection udpConnection;

    public UDPClient() throws IOException {
        channel = DatagramChannel.open();
        serverAddress = new InetSocketAddress(UDPConnection.HOST_NAME, UDPConnection.PORT);
        channel.socket().bind(new InetSocketAddress(0));
        udpConnection = new UDPConnection(channel, serverAddress);
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
}
