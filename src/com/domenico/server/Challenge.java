package com.domenico.server;

import com.domenico.communication.ConnectionData;

//TODO this doc
public class Challenge {

    //General info
    private final String from;
    private final String to;
    boolean playing;

    //Related to the request phase
    private final Object mutex = new Object();
    private boolean accepted;
    private boolean requestDone;
    boolean responseSent;
    private ConnectionData response;

    //Related to the words picking phase
    private String[] itWords;
    private String[] enWords;

    public Challenge(String from, String to) {
        this.from = from;
        this.to = to;
        this.init();
    }

    private void init() {
        this.accepted = false;
        this.requestDone = false;
        this.responseSent = false;
        this.playing = false;
        this.itWords = null;
        this.enWords = null;
        this.response = null;
    }

    public void setAccepted(boolean accepted) {
        synchronized (mutex) {
            if (requestDone) return;
            this.accepted = accepted;
            this.requestDone = true;
        }
    }

    /**
     * Returns if the challenge request has been sent and if the player replied or the request timedout.
     *
     * @return true if the challenge request has been replied by the user or if the request timedout
     */
    public boolean isRequestDone() {
        synchronized (mutex) {
            return requestDone;
        }
    }

    public boolean isAccepted() {
        synchronized (mutex) {
            return accepted;
        }
    }

    public void setWords(String[] itWords, String[] enWords) {
        synchronized (mutex) {
            if (this.itWords == null && this.enWords == null) {
                this.itWords = itWords;
                this.enWords = enWords;
            }
        }
    }

    public boolean hasWords() {
        synchronized (mutex) {
            return itWords != null && enWords != null;
        }
    }

    /**
     * Returns true if the user with the given username it's who sent this challenge request, false otherwise.
     *
     * @param username user's username to check
     * @return true if the user with the given username it's who sent this challenge request, false otherwise
     */
    public boolean isFromUser(String username) { return from.equals(username); }

    public String getFrom() { return from; }

    public String getTo() { return to; }

    public void setResponse(ConnectionData response) { this.response = response; }

    public ConnectionData getResponse() { return response; }

}
