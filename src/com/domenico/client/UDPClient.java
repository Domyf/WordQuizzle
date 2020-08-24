package com.domenico.client;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.UDPConnection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
//TODO fare questa documentazione
public class UDPClient implements Runnable {

    private DatagramChannel channel;
    private UDPConnection udpConnection;
    private WQClient wqClient;
    private OnChallengeArrivedListener onChallengeArrivedListener;  //listener da invocare quando arriva una nuova sfida

    public UDPClient(WQClient wqClient, OnChallengeArrivedListener listener) throws IOException {
        this.channel = DatagramChannel.open();
        this.channel.socket().bind(new InetSocketAddress(0));
        SocketAddress serverAddress = new InetSocketAddress(UDPConnection.HOST_NAME, UDPConnection.PORT);
        this.udpConnection = new UDPConnection(channel, serverAddress);
        this.onChallengeArrivedListener = listener;
        this.wqClient = wqClient;
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
                        udpConnection.sendData(ConnectionData.Factory.newFailResponse(""));
                }
            }
        } catch (IOException e) {

        }
    }
}
