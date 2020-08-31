package com.domenico.client;

import java.util.function.Consumer;

public interface ChallengeListener {

    /**
     * Invoked when a challenge request arrives. The caller also gives the consumer object that must be consumed when
     * the user accepts or declines a challenge.
     *
     * @param from who sent this challenge
     */
    void onChallengeArrived(String from);

    /**
     * Invoked when the challenge request has expired.
     */
    void onChallengeRequestTimeout();

    /**
     * Invoked during a challenge when it times out for example after 60 seconds
     */
    void onChallengeEnd();

}
