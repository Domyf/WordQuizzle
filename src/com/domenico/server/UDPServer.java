package com.domenico.server;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.UDPConnection;
import com.domenico.shared.Multiplexer;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UDPServer extends Multiplexer {

    private final UDPConnection udpConnection;
    private final Object mutex = new Object();
    private final List<Attachment> newRequests;

    private static class Attachment {
        ChallengeRequest challengeRequest;
        SocketAddress toAddress;
    }

    public UDPServer(DatagramChannel channel) throws IOException {
        super(channel, SelectionKey.OP_READ);
        this.udpConnection = new UDPConnection(channel, null);
        this.newRequests = new LinkedList<>();
    }

    public ChallengeRequest forwardChallenge(String from, String to, SocketAddress toAddress) throws ClosedChannelException {
        CompletableFuture<ChallengeRequest> future = new CompletableFuture<>();

        ChallengeRequest challengeRequest = new ChallengeRequest(from, to);
        Attachment attachment = new Attachment();
        attachment.challengeRequest = challengeRequest;
        attachment.toAddress = toAddress;
        synchronized (mutex) {
            newRequests.add(attachment);
        }
        wakeUp();
        return challengeRequest;
    }

    @Override
    protected void onWritable(SelectionKey key) throws IOException {
        Attachment att = (Attachment) key.attachment();
        ConnectionData data = ConnectionData.Factory.newChallengeRequest(att.challengeRequest.from, att.challengeRequest.to);
        udpConnection.sendData(data, att.toAddress);
        key.channel().register(selector, SelectionKey.OP_READ);
    }

    @Override
    protected void onReadable(SelectionKey key) throws IOException {
        ConnectionData response = udpConnection.receiveData();
        print(response.toString());
        key.cancel();
    }

    public void print(String str) {
        System.out.println("[UDP]: "+str);
    }

    @Override
    protected void onEndConnection(SelectionKey key) throws IOException {}

    @Override
    protected void onWakeUp() {}

    @Override
    protected void onAcceptable(SelectionKey key) throws IOException {}

    @Override
    protected void onTimeout() throws IOException {}
}
