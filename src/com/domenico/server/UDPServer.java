package com.domenico.server;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.UDPConnection;
import com.domenico.shared.Multiplexer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.*;

public class UDPServer extends Multiplexer implements Runnable {

    private final UDPConnection udpConnection;
    private final Object mutex = new Object();
    private final Map<InetSocketAddress, Challenge> mapAddress;

    private static class Forward {
        Challenge challenge;
        SocketAddress toAddress;

        public Forward(Challenge challenge, InetSocketAddress toAddress) {
            this.challenge = challenge;
            this.toAddress = toAddress;
        }
    }

    public UDPServer(DatagramChannel channel) throws IOException {
        super(channel, SelectionKey.OP_READ);
        channel.socket().bind(new InetSocketAddress(UDPConnection.PORT));
        this.udpConnection = new UDPConnection(channel, null);
        this.mapAddress = new HashMap<>();
    }

    public void forwardChallenge(Challenge challenge, InetSocketAddress toAddress) {
        synchronized (mutex) {
            mapAddress.put(toAddress, challenge);
            wakeUp();
        }
    }

    public void challengeTimedout(InetSocketAddress toAddress) {
        synchronized (mutex) {
            Challenge challenge = mapAddress.get(toAddress);
            //TODO fare la situazione del timedout
        }
    }

    @Override
    protected void onWritable(SelectionKey key) throws IOException {
        Challenge challenge = null;
        SocketAddress toAddress = null;
        boolean hasNext;
        synchronized (mutex) {
            Iterator<Map.Entry<InetSocketAddress, Challenge>> iterator = mapAddress.entrySet().iterator();
            if (iterator.hasNext()) {   //Sends the next challenge request
                Map.Entry<InetSocketAddress, Challenge> next = iterator.next();
                challenge = next.getValue();
                toAddress = next.getKey();
                //Removes this challenge from the once that should be sent
                iterator.remove();
            }
            hasNext = iterator.hasNext();
        }
        //Sends a new challenge request
        if (challenge != null && toAddress != null) {
            ConnectionData data = ConnectionData.Factory.newChallengeRequest(challenge.getFrom(), challenge.getTo());
            udpConnection.sendData(data, toAddress);
        }
        print("writable");
        //If there are no more challenges to forward then go read from the socket
        if (!hasNext) {
            channel.register(selector, SelectionKey.OP_READ);
            print("go read");
        }
    }

    @Override
    protected void onReadable(SelectionKey key) throws IOException {
        ConnectionData response = udpConnection.receiveData();
        InetSocketAddress address = (InetSocketAddress) udpConnection.getAddress();
        Challenge challenge;

        synchronized (mutex) {
            challenge = mapAddress.remove(address);
        }
        if (challenge != null) {    //if the challenge has not timedout
            print("readable");
            challenge.setResponse(response);
            print(response.toString());
        }
    }

    @Override
    protected void onWakeUp() {
        synchronized (mutex) {
            //If there is at least a request to forward, then go write into the socket
            if (!mapAddress.isEmpty()) {
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
