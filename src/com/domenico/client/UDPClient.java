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
    private OnChallengeArrivedListener onChallengeArrivedListener;

    public UDPClient(OnChallengeArrivedListener listener) throws IOException {
        channel = DatagramChannel.open();
        channel.socket().bind(new InetSocketAddress(0));
        SocketAddress serverAddress = new InetSocketAddress(UDPConnection.HOST_NAME, UDPConnection.PORT);
        udpConnection = new UDPConnection(channel, serverAddress);
        this.onChallengeArrivedListener = listener;
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
                    boolean accepted = onChallengeArrivedListener.onChallengeArrived(receiveData.getUsername());
                    if (accepted)
                        udpConnection.sendData(ConnectionData.Factory.newSuccessResponse());
                    else
                        udpConnection.sendData(ConnectionData.Factory.newFailResponse(loggedUsername+" ha rifiutato la sfida"));
                }
            }
        } catch (IOException e) {

        }
    }
}
