package com.domenico.client;

import java.util.function.Consumer;

public interface OnChallengeArrivedListener {

    /**
     * Invoked when a challenge request arrives. The caller also gives the consumer object that must be consumed when
     * the user accepts or declines a challenge.
     *
     * @param from who sent this challenge
     * @param onResponse The caller also gives the consumer object that must be consumed when the user accepts or
     *                   declines the challenge.
     */
    void onChallengeArrived(String from, Consumer<Boolean> onResponse);

    /**
     * Invoked when the challenge request has expired.
     */
    void onChallengeArrivedTimeout();

    void onChallengeEnd();
}
