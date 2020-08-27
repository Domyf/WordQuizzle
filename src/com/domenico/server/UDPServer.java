package com.domenico.server;

import com.domenico.communication.Connection;
import com.domenico.communication.ConnectionData;
import com.domenico.communication.UDPConnection;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

public class UDPServer extends Multiplexer {

    private final UDPConnection udpConnection;

    private static class Attachment {
        ChallengeRequest challengeRequest;
        SocketAddress toAddress;
    }

    public UDPServer(DatagramChannel channel) throws IOException {
        super(channel, SelectionKey.OP_READ);
        this.udpConnection = new UDPConnection(channel, null);
    }

    public ChallengeRequest forwardChallenge(String from, String to, SocketAddress toAddress) throws ClosedChannelException {
        ChallengeRequest challengeRequest = new ChallengeRequest(from, to);
        Attachment attachment = new Attachment();
        attachment.challengeRequest = challengeRequest;
        attachment.toAddress = toAddress;

        channel.register(selector, SelectionKey.OP_WRITE, attachment);
        return challengeRequest;
    }

    @Override
    protected void onTimeout() throws IOException {

    }

    @Override
    void onWritable(SelectionKey key) throws IOException {
        Attachment att = (Attachment) key.attachment();
        ConnectionData data = ConnectionData.Factory.newChallengeRequest(att.challengeRequest.from, att.challengeRequest.to);
        udpConnection.sendData(data, att.toAddress);
        key.channel().register(selector, SelectionKey.OP_READ);
    }

    @Override
    void onReadable(SelectionKey key) throws IOException {
        ConnectionData response = udpConnection.receiveData();
        print(response.toString());
        key.cancel();
    }

    public void print(String str) {
        System.out.println("[UDP]: "+str);
    }

    @Override
    void onEndConnection(SelectionKey key) throws IOException {}

    @Override
    protected void onWakeUp() {

    }

    @Override
    void onAcceptable(SelectionKey key) throws IOException {}
}
