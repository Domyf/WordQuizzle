package com.domenico.client;

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

    public void exit() throws IOException {
        udpConnection.endConnection();
    }

    public int getUDPPort() {
        return channel.socket().getLocalPort();
    }

    @Override
    public void run() {
        try {
            while (true) {
                ConnectionData receiveData = udpConnection.receiveData();
                if (ConnectionData.Validator.isChallengeRequest(receiveData)) {
                    System.out.println("Hai ricevuto una sfida da "+receiveData.getUsername());
                    Thread.sleep(8000);
                    //udpConnection.sendData(ConnectionData.Factory.newSuccessResponse());
                }
            }
        } catch (IOException | InterruptedException e) {

        }
    }
}
