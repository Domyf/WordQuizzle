package com.domenico.server;

public class ChallengeRequest {

    String from;
    String to;
    private boolean accepted;
    private boolean done;
    private final Object mutex = new Object();

    public ChallengeRequest(String from, String to) {
        this.from = from;
        this.to = to;
        this.accepted = false;
        this.done = false;
    }

    public void setAccepted(boolean accepted) {
        synchronized (mutex) {
            this.accepted = accepted;
            this.done = true;
        }
    }

    public boolean isDone() {
        synchronized (mutex) {
            return done;
        }
    }

    public boolean isAccepted() {
        synchronized (mutex) {
            return accepted;
        }
    }
}
