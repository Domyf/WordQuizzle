package com.domenico.server;

import com.domenico.shared.Utils;

import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Runnable that forwards a challenge request from the user A to the user B via UDP. After forwarding the request,
 * it waits for a response or the challenge timeout. If the challenge has been accepted by the challenged user, then
 * it gets random italian words and each translation. */
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
        //forward the challenge via udp
        udpServer.forwardChallenge(challenge, toUserAttachment.address);
        try {
            //wait the challenge response or timeout
            challenge.waitResponseOrTimeout(Settings.getChallengeRequestTimeout());
        } catch (InterruptedException e) { return; }
        //handle the response or the timeout
        handler.handleChallengeResponse(challenge, fromKey, toKey);
        //if the challenge is accepted, get random italian words and their english translations
        if (challenge.isRequestAccepted()) {
            //get random italian words
            List<String> itWords = new ArrayList<>(Settings.getChallengeWords());
            Utils.randomSubList(words, Settings.getChallengeWords(), itWords);
            //get the english translations
            List<String> enWords = new ArrayList<>(Arrays.asList(Translations.translate(itWords)));
            //print the selected words
            printSelectedWords(itWords, enWords);
            //update the challenge and notify that the words are ready
            challenge.setWords(itWords, enWords);
            handler.handleChallengeWordsReady(challenge, fromKey, toKey);
        }
    }

    /** Prints each italian word selected together with the translation got */
    private void printSelectedWords(List<String> itWords, List<String> enWords) {
        System.out.print("Selected words: [");
        for (int i = 0; i < Settings.getChallengeWords(); i++) {
            System.out.printf("(%s, %s)", itWords.get(i), enWords.get(i));
            if (i == Settings.getChallengeWords() - 1)
                System.out.println("]");
            else
                System.out.print(", ");
        }
    }
}
