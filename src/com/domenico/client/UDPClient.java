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

/**
 * This class extends the {@link Multiplexer} class and it handles all the UDP communications from and to the
 * WordQuizzle server. Such communications are all related to challenge requests. A challenge request is sent from the
 * server to a client via UDP. The client waits that the DatagramChannel is readable, and then it reads the challenge
 * request and sends it to the challenge handler (which is a {@link ChallengeListener}
 * that is passed when this class is instantiated). While the challenge handler will do its work, this thread comes back
 * to wait that the DatagramChannel is readable.
 * The UDPClient will then wake up when {@link #onChallengeResponse(Boolean)} is called by the challenge handler or
 * when the server sends that the challenge has timed out. On that case, the challenge handler is notified via
 * {@link ChallengeListener#onChallengeRequestTimeout()} and this thread comes back to wait messages from the
 * server.
 * One remote but still probable case is that a client accepts or declines a challenge while the server sends that
 * the challenge is timedout. On that case, the timeout event is more important and the user is notified that the
 * challenge has timedout without sending to the server that the user has accepted or declined the challenge.
 */
public class UDPClient extends Multiplexer implements Runnable {

    private final UDPConnection udpConnection;  //used to send and receive messages via DatagramChannel
    private final WQClient wqClient;
    private final int udpPort;  //port used to receive UDP data
    private boolean challengeTimeout = false;   //true if a challenge timeout has arrived, false otherwise
    private boolean challengeAccepted = false;  //true if the user has accepted the challenge, false otherwise

    /**
     * Sets up the UDPClient.
     *
     * @throws IOException when I/O error occurs
     */
    public UDPClient(WQClient wqClient) throws IOException {
        super(DatagramChannel.open(), SelectionKey.OP_READ);
        DatagramChannel channel = (DatagramChannel) super.channel;
        channel.socket().bind(new InetSocketAddress(0));    //bind with ephemeral port
        SocketAddress serverAddress = new InetSocketAddress(UDPConnection.HOST_NAME, UDPConnection.PORT);
        this.udpConnection = new UDPConnection(channel, serverAddress);
        this.wqClient = wqClient;
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

    /**
     * Sends to the server if the challenge has been accepted or declined by the client. Then it comes back to read
     * from the channel.
     *
     * @param key the selection key relative to that channel on which the write() method will not block the thread
     * @throws IOException when I/O error occurs
     */
    @Override
    protected void onWritable(SelectionKey key) throws IOException {
        //sends if the challenge has been accepted or declined
        if (challengeAccepted)
            udpConnection.sendData(ConnectionData.Factory.newSuccessResponse());
        else
            udpConnection.sendData(ConnectionData.Factory.newFailResponse());

        //comes back to read from the channel
        channel.register(selector, SelectionKey.OP_READ);
    }

    @Override
    protected void onReadable(SelectionKey key) throws IOException {
        ConnectionData data = udpConnection.receiveData();

        if (ConnectionData.Validator.isChallengeRequest(data)) {
            challengeTimeout = false;
            wqClient.onChallengeArrived(data.getUsername());
        } else if (ConnectionData.Validator.isFailResponse(data)) {
            challengeTimeout = true;
            wqClient.onChallengeRequestTimeout();
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
    public void onChallengeResponse(Boolean accepted) {
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
    protected void onEndConnection(SelectionKey key) throws IOException {
        this.stopProcessing();
    }

    @Override
    protected void onTimeout() throws IOException { }

    @Override
    protected void onAcceptable(SelectionKey key) throws IOException { }
}
