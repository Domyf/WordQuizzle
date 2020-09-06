package com.domenico.server.network;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.UDPConnection;
import com.domenico.server.Challenge;
import com.domenico.shared.Multiplexer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.*;

/** This class extends the {@link Multiplexer} class and it handles all the UDP communications from and to the clients.
 * Basically it forwards the given challenge requests from a client to another and waits that the other user sends a
 * challenge response or that the challenge request times out.
 */
public class UDPServer extends Multiplexer implements Runnable {

    //Utility object that wraps all the work that should be done to send or receive a ConnectionData object
    private final UDPConnection udpConnection;
    //map object that maps each address to a challenge object
    private final Map<InetSocketAddress, Challenge> mapAddress;
    //A linked list used to store all the challenges that should be forwarded
    private final Object mutex = new Object();
    private final LinkedList<Forward> forwards;

    /** Inner class used to represent a challenge that should be forwarded to a given address via UDP */
    private static class Forward {
        InetSocketAddress toAddress;
        Challenge challenge;

        public Forward(InetSocketAddress toAddress, Challenge challenge) {
            this.toAddress = toAddress;
            this.challenge = challenge;
        }
    }

    public UDPServer() throws IOException {
        super(DatagramChannel.open(), SelectionKey.OP_READ);
        DatagramChannel datagramChannel = (DatagramChannel) channel;
        datagramChannel.socket().bind(new InetSocketAddress(UDPConnection.PORT));
        this.udpConnection = new UDPConnection(datagramChannel, null);
        this.mapAddress = new HashMap<>();
        this.forwards = new LinkedList<>();
    }

    /** Forwards the given challenge to the user (specified by its address) via UDP */
    public void forwardChallenge(Challenge challenge, InetSocketAddress toAddress) {
        handleChallenge(challenge, toAddress);
    }

    /** Sends to the challenged user that the challenge request has timed out */
    public void challengeTimedout(Challenge challenge, InetSocketAddress toAddress) {
        handleChallenge(challenge, toAddress);
    }

    /** Handles a new challenge that should be forwarded or a challenge request timeout */
    private void handleChallenge(Challenge challenge, InetSocketAddress toAddress) {
        synchronized (mutex) {
            forwards.push(new Forward(toAddress, challenge));
            wakeUp();
        }
    }

    @Override
    protected void onWritable(SelectionKey key) throws IOException {
        boolean isEmpty;
        Forward forward = null;
        //Pop the next forward
        synchronized (mutex) {
            if (!forwards.isEmpty()) {
                forward = forwards.pop();
            }
            isEmpty = forwards.isEmpty();
        }

        //Sends a new challenge request or sends that the challenge timed out
        if (forward != null) {
            ConnectionData data;
            if (forward.challenge.isRequestTimedOut()) {
                mapAddress.remove(forward.toAddress, forward.challenge);
                data = ConnectionData.Factory.newFailResponse("Tempo scaduto");
            } else {
                mapAddress.put(forward.toAddress, forward.challenge);
                data = ConnectionData.Factory.newChallengeRequest(forward.challenge.getFrom(), forward.challenge.getTo());
            }

            udpConnection.sendData(data, forward.toAddress);
            print(data.toString(), forward.toAddress, forward.challenge.getTo());
        }

        //If there are no more challenges to forward then go read from the socket
        if (isEmpty) {
            channel.register(selector, SelectionKey.OP_READ);
        }
    }

    @Override
    protected void onReadable(SelectionKey key) throws IOException {
        ConnectionData response = udpConnection.receiveData();
        InetSocketAddress address = (InetSocketAddress) udpConnection.getAddress();
        Challenge challenge = mapAddress.remove(address);
        challenge.setRequestAccepted(ConnectionData.Validator.isSuccessResponse(response));
    }

    @Override
    protected void onWakeUp() {
        synchronized (mutex) {
            //If there is at least a request to forward, then go write into the socket
            if (!forwards.isEmpty()) {
                try {
                    channel.register(selector, SelectionKey.OP_WRITE);
                } catch (ClosedChannelException ignored) { }
            }
        }
    }

    @Override
    public void run() {
        System.out.println("[UDP]: Server is running");
        this.startProcessing();
    }

    /** Prints information about the challenge that has been forwarded */
    public void print(String str, InetSocketAddress toAddress, String username) {
        System.out.printf("[UDP]: %s -> %s (%s)\n", str, toAddress, username);
    }

    @Override
    protected void onEndConnection(SelectionKey key) throws IOException {}

    @Override
    protected void onAcceptable(SelectionKey key) throws IOException {}

    @Override
    protected void onTimeout() throws IOException {}
}
