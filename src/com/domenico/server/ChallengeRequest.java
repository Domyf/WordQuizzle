package com.domenico.server;

import java.io.IOException;
import java.net.InetSocketAddress;

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
            challenge.waitResponseOrTimeout(MAX_WAITING_TIME);
        } catch (InterruptedException e) { return; }

        //If the challenge request has timedout
        if (challenge.isTimedout()) {
            udpServer.challengeTimedout(challenge, toAddress);
        } else if (challenge.isAccepted()) {
            //If the challenge is accepted, get random italian words and their english translations
            //TODO rimuovere questo codice di test
            String[] itWords = {"Cane", "Gatto", "Canzone"};
            String[] enWords = Translations.translate(itWords);
            for (String translation : enWords) {
                System.out.print(translation);
            }
            System.out.println();
            challenge.setWords(itWords, enWords);
        }
    }

    public void print(String str) { System.out.println("[UDP]: "+str); }

}
