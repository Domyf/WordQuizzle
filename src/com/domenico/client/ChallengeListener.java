package com.domenico.client;

/** Interface with all the methods relative to the async events that can happen in case of a challenge request */
public interface ChallengeListener {

    /**
     * Invoked when a challenge request arrives. The caller also gives the consumer object that must be consumed when
     * the user accepts or declines a challenge.
     * @param from who sent this challenge
     */
    void onChallengeArrived(String from);

    /** Invoked when the challenge request has expired */
    void onChallengeRequestTimeout();

    /**
     * Invoked during a challenge when it times out for example after 60 seconds or when both users send the last
     * translation.
     * @param correct how many correct translations
     * @param wrong how many wrong translations
     * @param notransl how many words the user didn't translate
     * @param yourscore score set by the user
     * @param otherscore score set by the user's friend
     * @param extrapoints the extrapoint given to the user. 0 in case che user has lost
     */
    void onChallengeEnd(int correct, int wrong, int notransl, int yourscore, int otherscore, int extrapoints);

}
