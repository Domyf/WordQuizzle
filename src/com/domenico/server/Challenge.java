package com.domenico.server;

import java.util.List;

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
    private List<String> itWords = null;
    private List<String> enWords = null;
    private int fromIndex;
    private int toIndex;
    private int fromPoints;
    private int toPoints;

    public Challenge(String from, String to) {
        this.from = from;
        this.to = to;
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

    public void setWords(List<String> itWords, List<String> enWords) {
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

    public String getNextItWord(String username) {
        if (username.equals(from))
            return itWords.get(fromIndex);
        if (username.equals(to))
            return itWords.get(toIndex);
        return null;
    }

    public boolean checkAndGoNext(String username, String enWord) {
        String rightEnWord;
        if (username.equals(from)) {
            rightEnWord = itWords.get(fromIndex);
            fromIndex++;
        } else if (username.equals(to)) {
            rightEnWord = itWords.get(toIndex);
            toIndex++;
        } else {
            return false;
        }

        boolean isRight = rightEnWord.equalsIgnoreCase(enWord);
        if (isRight)
            addPoints(username, Settings.POINTS_RIGHT_TRANSLATION);
        else
            subtractPoints(username, Settings.POINTS_ERROR_PENALTY);

        return isRight;
    }

    public boolean hasPlayerEnded(String username) {
        if (username.equals(from)) {
            return fromIndex == itWords.size();
        } else if (username.equals(to)) {
            return toIndex == itWords.size();
        }
        return true;
    }

    public boolean isGameEnded() {
        //return challengeTimeout || (hasPlayerEnded(from) && hasPlayerEnded(to));
        return (hasPlayerEnded(from) && hasPlayerEnded(to));
    }

    private void addPoints(String username, int amount) {
        if (username.equals(from)) {
            fromPoints = fromPoints + amount;
        } else if (username.equals(to)) {
            toPoints = toPoints + amount;
        }
    }

    private void subtractPoints(String username, int amount) {
        if (username.equals(from)) {
            fromPoints = fromPoints - amount;
        } else if (username.equals(to)) {
            toPoints = toPoints - amount;
        }
    }

}
