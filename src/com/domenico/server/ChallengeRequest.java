package com.domenico.server;

import com.domenico.shared.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//TODO this doc
public class ChallengeRequest implements Runnable {

    private final Challenge challenge;
    private final InetSocketAddress toAddress;
    private final UDPServer udpServer;
    private final List<String> words;

    public ChallengeRequest(UDPServer udpServer, InetSocketAddress remoteAddress, Challenge challenge, List<String> words) throws IOException {
        this.udpServer = udpServer;
        this.challenge = challenge;
        this.toAddress = remoteAddress;
        this.words = words;
    }

    @Override
    public void run() {
        print("Forwarding challenge request from "+ challenge.getFrom()+" to "+ challenge.getTo());
        udpServer.forwardChallenge(challenge, toAddress);
        try {
            challenge.waitResponseOrTimeout(Settings.MAX_WAITING_TIME);
        } catch (InterruptedException e) { return; }

        //If the challenge request has timedout
        if (challenge.isTimedout()) {
            udpServer.challengeTimedout(challenge, toAddress);
        } else if (challenge.isAccepted()) {
            //If the challenge is accepted, get random italian words and their english translations
            List<String> itWords = new ArrayList<>(Settings.CHALLENGE_WORDS);
            Utils.randomSubList(words, Settings.CHALLENGE_WORDS, itWords);
            List<String> enWords = Arrays.asList(Translations.translate(itWords));
            for (String translation : enWords) {
                System.out.print(translation);
            }
            System.out.println();
            challenge.setWords(itWords, enWords);
        }
    }

    public void print(String str) { System.out.println("[UDP]: "+str); }

}
