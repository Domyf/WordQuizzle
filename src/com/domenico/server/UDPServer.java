package com.domenico.server;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.UDPConnection;
import com.domenico.shared.Multiplexer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.*;

public class UDPServer extends Multiplexer implements Runnable {

    private final UDPConnection udpConnection;
    private final Object mutex = new Object();
    private final Map<InetSocketAddress, Challenge> mapAddress;
    private final LinkedList<Forward> forwards;

    private static class Forward {
        InetSocketAddress toAddress;
        Challenge challenge;

        public Forward(InetSocketAddress toAddress, Challenge challenge) {
            this.toAddress = toAddress;
            this.challenge = challenge;
        }
    }

    public UDPServer(DatagramChannel channel) throws IOException {
        super(channel, SelectionKey.OP_READ);
        channel.socket().bind(new InetSocketAddress(UDPConnection.PORT));
        this.udpConnection = new UDPConnection(channel, null);
        this.mapAddress = new HashMap<>();
        this.forwards = new LinkedList<>();
    }

    public void forwardChallenge(Challenge challenge, InetSocketAddress toAddress) {
        handleChallenge(challenge, toAddress);
    }

    public void challengeTimedout(Challenge challenge, InetSocketAddress toAddress) {
        handleChallenge(challenge, toAddress);
    }

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

        //Sends a new challenge request or sends that the challenge timedout
        if (forward != null) { //TODO check is challenge timedout
            Challenge challenge = forward.challenge;
            ConnectionData data;
            if (challenge.isTimedout()) {
                mapAddress.remove(forward.toAddress, forward.challenge);
                data = ConnectionData.Factory.newFailResponse("Tempo scaduto");
            } else {
                mapAddress.put(forward.toAddress, forward.challenge);
                data = ConnectionData.Factory.newChallengeRequest(challenge.getFrom(), challenge.getTo());
            }

            udpConnection.sendData(data, forward.toAddress);
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
        challenge.setAccepted(ConnectionData.Validator.isSuccessResponse(response));
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
        this.startProcessing();
    }
    
    public void print(String str) {
        System.out.println("[UDP]: "+str);
    }

    @Override
    protected void onEndConnection(SelectionKey key) throws IOException {}

    @Override
    protected void onAcceptable(SelectionKey key) throws IOException {}

    @Override
    protected void onTimeout() throws IOException {}
}
