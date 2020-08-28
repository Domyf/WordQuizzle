package com.domenico.server;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.UDPConnection;
import com.domenico.shared.Multiplexer;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.Callable;

public class UDPRequest extends Multiplexer implements Callable<ConnectionData> {

    private static final int MAX_WAITING_TIME = 5000;
    private final ChallengeRequest challengeRequest;
    private final UDPConnection udpConnection;
    private ConnectionData response;

    public UDPRequest(DatagramChannel channel, SocketAddress remoteAddress, ChallengeRequest challengeRequest) throws IOException {
        super(channel, SelectionKey.OP_WRITE, MAX_WAITING_TIME);
        this.udpConnection = new UDPConnection(channel, remoteAddress);
        this.challengeRequest = challengeRequest;
    }

    @Override
    public ConnectionData call() throws Exception {
        response = null;
        print("Forwarding challenge request from "+challengeRequest.from+" to "+challengeRequest.to);
        //loops until time is out or connection is closed
        super.startProcessing();
        if (response == null) {
            response = ConnectionData.Factory.newFailResponse("Tempo scaduto");
            udpConnection.sendData(response);   //Send time is up to who received the challenge
        }
        if (ConnectionData.Validator.isSuccessResponse(response)) {
            challengeRequest.setAccepted(true);
        } else if (ConnectionData.Validator.isFailResponse(response)) {
            challengeRequest.setAccepted(false);
        }
        //TODO rimuovere questo codice di test
        /*String[] words = {"Cane", "Gatto", "Canzone"};
        String[] translations = Translations.translate(words);
        for (String translation : translations) {
            System.out.print(translation);
        }
        System.out.println();*/
        return response;
    }

    @Override
    protected void onTimeout() throws IOException {
        super.stopProcessing();
    }

    @Override
    protected void onWritable(SelectionKey key) throws IOException {
        udpConnection.sendData(ConnectionData.Factory.newChallengeRequest(challengeRequest.from, challengeRequest.to));
        key.channel().register(selector, SelectionKey.OP_READ);
    }

    @Override
    protected void onReadable(SelectionKey key) throws IOException {
        response = udpConnection.receiveData();
        print(response.toString());
        super.stopProcessing();
    }

    @Override
    protected void onEndConnection(SelectionKey key) throws IOException {
        super.stopProcessing();
    }

    public void print(String str) { System.out.println("[UDP]: "+str); }

    @Override
    protected void onWakeUp() {}

    @Override
    protected void onAcceptable(SelectionKey key) throws IOException {}

}
