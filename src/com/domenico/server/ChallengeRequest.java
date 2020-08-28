package com.domenico.server;

public class ChallengeRequest {

    String from;
    String to;
    private boolean accepted;
    private boolean hasReplied;
    private boolean done;
    private final Object mutex = new Object();

    public ChallengeRequest(String from, String to) {
        this.from = from;
        this.to = to;
        this.init();
    }

    private void init() {
        this.accepted = false;
        this.hasReplied = false;
        this.done = false;
    }

    public void reset() {
        synchronized (mutex) {
            init();
        }
    }

    public void setAccepted(boolean accepted) {
        synchronized (mutex) {
            if (hasReplied)
                return;
            this.accepted = accepted;
            this.hasReplied = true;
            this.done = true;
        }
    }

    public boolean isAccepted() {
        synchronized (mutex) {
            return accepted;
        }
    }

    public boolean isDone() {
        synchronized (mutex) {
            return done;
        }
    }
}
