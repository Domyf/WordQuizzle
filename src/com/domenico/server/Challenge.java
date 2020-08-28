package com.domenico.server;

//TODO this doc
public class Challenge {

    //General info
    private final String from;
    private final String to;
    boolean playing;

    //Related to the request phase
    private final Object mutex = new Object();
    private boolean responseArrived;
    private boolean responseSentBack;
    private boolean accepted;
    private boolean timedout;

    //Related to the words picking phase
    private String[] itWords;
    private String[] enWords;

    public Challenge(String from, String to) {
        this.from = from;
        this.to = to;
        this.responseArrived = false;
        this.responseSentBack = false;
        this.accepted = false;
        this.timedout = false;
        this.playing = false;
        this.itWords = null;
        this.enWords = null;
    }

    /**
     * Returns if the other player has replied to the challenge request or the request has timedout.
     *
     * @return true if the challenge request has been replied by the user or if the request timedout
     */
    public boolean hasResponseOrTimeout() {
        synchronized (mutex) {
            return responseArrived || timedout;
        }
    }

    public void waitResponseOrTimeout(long millis) throws InterruptedException {
        synchronized (mutex) {
            long now = System.currentTimeMillis();
            long end = now + millis;
            while (!responseArrived && now < end) {
                mutex.wait(end - now);
                now = System.currentTimeMillis();
            }

            if (!responseArrived) { //timeout
                timedout = true;
                accepted = false;
            }
        }
    }

    public void setAccepted(boolean accepted) {
        synchronized (mutex) {
            if (this.responseArrived || this.timedout)
                return;

            this.accepted = accepted;
            this.responseArrived = true;
            mutex.notify();
        }
    }

    public boolean isAccepted() {
        synchronized (mutex) {
            return accepted;
        }
    }

    public boolean isTimedout() {
        synchronized (mutex) {
            return timedout;
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

    public boolean isResponseSentBack() { return responseSentBack; }

    public void setResponseSentBack(boolean responseSentBack) { this.responseSentBack = responseSentBack; }


}
