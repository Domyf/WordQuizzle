package com.domenico.client;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.UDPConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

public class UDPClient extends Thread {

    private DatagramChannel channel;
    private UDPConnection udpConnection;
    private String loggedUsername;

    public UDPClient() throws IOException {
        this.channel = DatagramChannel.open();
        this.channel.socket().bind(new InetSocketAddress(0));
        SocketAddress serverAddress = new InetSocketAddress(UDPConnection.HOST_NAME, UDPConnection.PORT);
        udpConnection = new UDPConnection(channel, serverAddress);
    }

    public boolean init(String username) throws IOException {
        ConnectionData connectionData = ConnectionData.Factory.newLoginRequest(username, "placeholderPassword");
        udpConnection.sendData(connectionData);
        ConnectionData response = udpConnection.receiveData();
        if (ConnectionData.Validator.isSuccessResponse(response))
            return true;
        return false;
    }

    public void exit() throws IOException {
        udpConnection.closeConnection();
    }

    @Override
    public void run() {
        try {
            if (init(loggedUsername))
                System.out.println("Connesso");
            while(true) {
                ConnectionData connectionData = udpConnection.receiveData();
                if (ConnectionData.Validator.isChallengeRequest(connectionData)) {
                    System.out.println("Ricevuta sfida da "+connectionData.getUsername());
                    ConnectionData response = ConnectionData.Factory.newSuccessResponse();
                    udpConnection.sendData(response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLoggedUsername(String loggedUsername) {
        this.loggedUsername = loggedUsername;
    }
}
