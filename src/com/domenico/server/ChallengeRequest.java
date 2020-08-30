package com.domenico.server;

import com.domenico.shared.Utils;

import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//TODO this doc
public class ChallengeRequest implements Runnable {

    private final UDPServer udpServer;
    private final List<String> words;
    private final SelectionKey fromKey;
    private final SelectionKey toKey;
    private final WQHandler handler;

    public ChallengeRequest(WQHandler handler, UDPServer udpServer, SelectionKey fromKey, SelectionKey toKey, List<String> words) {
        this.udpServer = udpServer;
        this.fromKey = fromKey;
        this.toKey = toKey;
        this.words = words;
        this.handler = handler;
    }

    @Override
    public void run() {
        TCPServer.Attachment toUserAttachment = (TCPServer.Attachment) toKey.attachment();
        Challenge challenge = toUserAttachment.challenge;
        print("Forwarding challenge request from " + challenge.getFrom() + " to " + challenge.getTo());
        udpServer.forwardChallenge(challenge, toUserAttachment.address);
        try {
            challenge.waitResponseOrTimeout(Settings.MAX_WAITING_TIME);
        } catch (InterruptedException e) { return; }
        handler.handleChallengeResponse(challenge, fromKey, toKey);
        if (challenge.isAccepted()) {
            //If the challenge is accepted, get random italian words and their english translations
            List<String> itWords = new ArrayList<>(Settings.CHALLENGE_WORDS);
            Utils.randomSubList(words, Settings.CHALLENGE_WORDS, itWords);
            List<String> enWords = Arrays.asList(Translations.translate(itWords));
            for (String translation : enWords) {
                System.out.print(translation);
            }
            System.out.println();
            challenge.setWords(itWords, enWords);
            handler.handleChallengeWordsReady(challenge, fromKey, toKey);
        }
    }

    public void print(String str) { System.out.println("[UDP]: "+str); }

}
