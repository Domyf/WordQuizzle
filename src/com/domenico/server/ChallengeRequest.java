package com.domenico.server;

import com.domenico.communication.ConnectionData;
import com.domenico.communication.UDPConnection;
import com.domenico.shared.Multiplexer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

//TODO this doc
public class ChallengeRequest implements Runnable {

    private static final int MAX_WAITING_TIME = 5000;
    private final Challenge challenge;
    private final InetSocketAddress toAddress;
    private final UDPServer udpServer;

    public ChallengeRequest(UDPServer udpServer, InetSocketAddress remoteAddress, Challenge challenge) throws IOException {
        this.udpServer = udpServer;
        this.challenge = challenge;
        this.toAddress = remoteAddress;
    }

    @Override
    public void run() {
        print("Forwarding challenge request from "+ challenge.getFrom()+" to "+ challenge.getTo());
        udpServer.forwardChallenge(challenge, toAddress);
        try {
            Thread.sleep(MAX_WAITING_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //If response is null then the user never replied and the time is up
        if (challenge.getResponse() == null) {
            System.out.println("tempo scaduto");    //TODO fare il caso timedout e rimuovere la println
            /*challenge.setResponse(ConnectionData.Factory.newFailResponse("Tempo scaduto"));
            try {
                //channel.configureBlocking(false);
                udpConnection.sendData(response);   //Send time is up to who received the challenge
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            challenge.setAccepted(false);
        } else if (ConnectionData.Validator.isSuccessResponse(challenge.getResponse())) {
            challenge.setAccepted(true);
            //TODO rimuovere questo codice di test
            String[] itWords = {"Cane", "Gatto", "Canzone"};
            String[] enWords = Translations.translate(itWords);
            for (String translation : enWords) {
                System.out.print(translation);
            }
            System.out.println();
            challenge.setWords(itWords, enWords);
        } else if (ConnectionData.Validator.isFailResponse(challenge.getResponse())) {
            challenge.setAccepted(false);
        }
    }

    public void print(String str) { System.out.println("[UDP]: "+str); }

}
