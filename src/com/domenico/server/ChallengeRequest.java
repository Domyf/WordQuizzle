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
        System.out.println("Forwarding challenge request from " + challenge.getFrom() + " to " + challenge.getTo());
        udpServer.forwardChallenge(challenge, toUserAttachment.address);
        try {
            challenge.waitResponseOrTimeout(Settings.MAX_WAITING_TIME);
        } catch (InterruptedException e) { return; }
        handler.handleChallengeResponse(challenge, fromKey, toKey);
        if (challenge.isAccepted()) {
            //If the challenge is accepted, get random italian words and their english translations
            List<String> itWords = new ArrayList<>(Settings.CHALLENGE_WORDS);
            Utils.randomSubList(words, Settings.CHALLENGE_WORDS, itWords);
            List<String> enWords = new ArrayList<>(Arrays.asList(Translations.translate(itWords)));
            printSelectedWords(itWords, enWords);
            challenge.setWords(itWords, enWords);
            handler.handleChallengeWordsReady(challenge, fromKey, toKey);
        }
    }

    /** Prints each italian word selected together with the translation got */
    private void printSelectedWords(List<String> itWords, List<String> enWords) {
        System.out.print("Selected words: [");
        for (int i = 0; i < Settings.CHALLENGE_WORDS; i++) {
            System.out.printf("(%s, %s)", itWords.get(i), enWords.get(i));
            if (i == Settings.CHALLENGE_WORDS - 1)
                System.out.println("]");
            else
                System.out.print(", ");
        }
    }
}
