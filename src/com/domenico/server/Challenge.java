package com.domenico.server;

import java.util.List;
import java.util.TimerTask;

//TODO this doc
public class Challenge {

    //General info
    private final String from;
    private final String to;

    //Related to the request phase
    private final Object mutex = new Object();
    private boolean responseArrived;
    private boolean accepted;
    private boolean timedOut;
    private boolean ended;

    //Related to the gaming phase
    private List<String> itWords = null;
    private List<String> enWords = null;
    private int fromIndex;
    private int toIndex;
    private int fromPoints;
    private int toPoints;
    private TimerTask timer;

    public Challenge(String from, String to) {
        this.from = from;
        this.to = to;
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
                timedOut = true;
                accepted = false;
            }
        }
    }

    public void setAccepted(boolean accepted) {
        synchronized (mutex) {
            if (this.responseArrived || this.timedOut)
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

    public boolean isTimedOut() {
        synchronized (mutex) {
            return timedOut;
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

    public String getFrom() { return from; }

    public String getTo() { return to; }

    public String getNextItWord(String username) {
        if (username.equals(from) && fromIndex < itWords.size())
            return itWords.get(fromIndex);
        if (username.equals(to) && toIndex < itWords.size())
            return itWords.get(toIndex);
        return null;
    }

    public void checkAndGoNext(String username, String enWord) {
        String rightEnWord = null;
        if (username.equals(from)) {
            rightEnWord = itWords.get(fromIndex);
            fromIndex++;
        } else if (username.equals(to)) {
            rightEnWord = itWords.get(toIndex);
            toIndex++;
        }

        boolean isRight = rightEnWord != null && rightEnWord.equalsIgnoreCase(enWord);
        if (isRight)
            addPoints(username, Settings.POINTS_RIGHT_TRANSLATION);
        else
            subtractPoints(username, Settings.POINTS_ERROR_PENALTY);
        if (hasPlayerEnded(from) && hasPlayerEnded(to))
            onChallengeEnded();
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
        return ended || (hasPlayerEnded(from) && hasPlayerEnded(to));
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

    public void onChallengeEnded() {
        if (this.ended) return;
        this.ended = true;
        if (fromPoints > toPoints)
            addPoints(from, Settings.EXTRA_POINTS);
        else if (toPoints > fromPoints)
            addPoints(to, Settings.EXTRA_POINTS);
    }

    public void setTimer(TimerTask timer) {
        this.timer = timer;
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
