package com.domenico.client;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.UDPConnection;
import com.domenico.shared.Multiplexer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

//TODO fare questa documentazione
public class UDPClient extends Multiplexer implements Runnable {

    private final UDPConnection udpConnection;  //used to send and receive messages via DatagramChannel
    private final OnChallengeArrivedListener onChallengeArrivedListener;  //listener used to signal the events relative to the challenge request
    private final int udpPort;  //port used to receive UDP data
    private boolean challengeTimeout = false;   //true if a challenge timeout has arrived, false otherwise
    private boolean challengeAccepted = false;  //true if the user has accepted the challenge, false otherwise

    public UDPClient(OnChallengeArrivedListener listener) throws IOException {
        super(DatagramChannel.open(), SelectionKey.OP_READ);
        DatagramChannel channel = (DatagramChannel) super.channel;
        channel.socket().bind(new InetSocketAddress(0));    //bind with ephemeral port
        SocketAddress serverAddress = new InetSocketAddress(UDPConnection.HOST_NAME, UDPConnection.PORT);
        this.udpConnection = new UDPConnection(channel, serverAddress);
        this.onChallengeArrivedListener = listener;
        this.udpPort = channel.socket().getLocalPort();
    }

    /**
     * Returns the UDP port used by this thread for UDP communications with the WordQuizzle server.
     *
     * @return the ephemeral port used by this thread for UDP communications
     */
    public int getUDPPort() { return udpPort; }

    @Override
    public void run() {
        this.startProcessing();
    }

    @Override
    protected void onWritable(SelectionKey key) throws IOException {
        //sends if the challenge has been accepted or declined
        if (challengeAccepted)
            udpConnection.sendData(ConnectionData.Factory.newSuccessResponse());
        else
            udpConnection.sendData(ConnectionData.Factory.newFailResponse());

        channel.register(selector, SelectionKey.OP_READ);
    }

    
    @Override
    protected void onReadable(SelectionKey key) throws IOException {
        ConnectionData data = udpConnection.receiveData();
        if (ConnectionData.Validator.isChallengeRequest(data)) {
            challengeTimeout = false;
            onChallengeArrivedListener.onChallengeArrived(data.getUsername(), this::onChallengeResponse);
        } else if (ConnectionData.Validator.isFailResponse(data)) { //TODO change into ChallengeRequestTimeout
            challengeTimeout = true;
            onChallengeArrivedListener.onChallengeArrivedTimeout();
        }
        channel.register(selector, SelectionKey.OP_READ);
    }

    /**
     * Called when the challenge has been accepted or declined by the client. It wakes up the thread if it is waiting
     * by the effects of a {@link Selector#select()} call. On wake up, the thread will first do reading operations and
     * then it is possible to receive a challenge timeout before sending that the challenge has been accepted or declined.
     *
     * @param accepted true is the user has accepted, false otherwise
     */
    private void onChallengeResponse(Boolean accepted) {
        this.challengeAccepted = accepted;
        wakeUp();
    }

    /**
     * Called if the thread has been woke up. When the client accepts or decline a challenge, the method
     * {@link #onChallengeResponse(Boolean)} is called and it will wake up the thread. On wake up, this method is called
     * after handling some readable and writable keys, so it is possible that a challenge timeout has arrived. In that
     * case the challenge response is not sent.
     */
    @Override
    protected void onWakeUp() {
        try {
            if (!challengeTimeout) {
                channel.register(selector, SelectionKey.OP_WRITE);
            }
        } catch (ClosedChannelException ignored) {}
    }

    @Override
    protected void onEndConnection(SelectionKey key) throws IOException { this.stopProcessing(); }

    @Override
    protected void onTimeout() throws IOException { }

    @Override
    protected void onAcceptable(SelectionKey key) throws IOException { }
}
