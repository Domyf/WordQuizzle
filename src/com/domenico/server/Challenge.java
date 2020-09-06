package com.domenico.server;

import java.util.List;
import java.util.TimerTask;

/** Implementation of a Word Quizzle challenge. */
public class Challenge {

    //General info
    private final String from;
    private final String to;

    //Related to the request phase
    private final Object mutex = new Object();
    private boolean responseArrived;    //true if the challenge request has a response
    private boolean requestAccepted;    //true if the challenge request has been accepted
    private boolean requestTimedOut;    //true if the challenge request timed out

    //Related to the gaming phase
    private List<String> itWords = null;
    private List<String> enWords = null;
    private boolean ended;  //true if the challenge is ended
    private int fromIndex;  //index to the next word
    private int toIndex;
    private int fromPoints; //points scores
    private int toPoints;
    private int fromRight;  //how many words have been translated correctly
    private int toRight;
    private int fromWrong;  //how many bad translations has the user sent
    private int toWrong;
    private TimerTask timer;    //timer that handle the challenge timeout

    public Challenge(String from, String to) {
        this.from = from;
        this.to = to;
    }

    /** Waits that the challenge response arrives or until the challenge times out. It doesn't wait more than the given
     * milliseconds
     * @param millis how many milliseconds is the timeout long
     * @throws InterruptedException if any thread interrupted the current thread before or while the current thread was
     * waiting.
     */
    public void waitResponseOrTimeout(long millis) throws InterruptedException {
        synchronized (mutex) {
            long now = System.currentTimeMillis();
            long end = now + millis;
            while (!responseArrived && now < end) {
                mutex.wait(end - now);
                now = System.currentTimeMillis();
            }

            if (!responseArrived) { //timeout
                requestTimedOut = true;
                requestAccepted = false;
            }
        }
    }

    /** Sets if the challenge request has been accepted or declined */
    public void setRequestAccepted(boolean requestAccepted) {
        synchronized (mutex) {
            if (this.responseArrived || this.requestTimedOut)
                return;

            this.requestAccepted = requestAccepted;
            this.responseArrived = true;
            mutex.notify();
        }
    }

    /** Returns true if the challenge request has been accepted, false otherwise */
    public boolean isRequestAccepted() {
        synchronized (mutex) {
            return requestAccepted;
        }
    }

    /** Returns true if the challenge request has timed out, false otherwise */
    public boolean isRequestTimedOut() {
        synchronized (mutex) {
            return requestTimedOut;
        }
    }

    /** Sets all the italian words and each translation */
    public void setWords(List<String> itWords, List<String> enWords) {
        synchronized (mutex) {
            if (this.itWords == null && this.enWords == null) {
                this.itWords = itWords;
                this.enWords = enWords;
            }
        }
    }

    /** Returns who sent the challenge */
    public String getFrom() { return from; }

    /** Returns who was challenged */
    public String getTo() { return to; }

    /** Returns the next italian word that the given user should translate */
    public String getNextItWord(String username) {
        if (username.equals(from) && fromIndex < itWords.size())
            return itWords.get(fromIndex);
        if (username.equals(to) && toIndex < itWords.size())
            return itWords.get(toIndex);
        return null;
    }

    /** Checks if the given user has given the right translation or not. It updates the user's score and then it goes
     * to the next word.
     * @param username user's username
     * @param enWord the translation given by the user for its current word
     */
    public void checkAndGoNext(String username, String enWord) {
        boolean isRight;
        String rightEnWord;
        if (username.equals(from)) {
            rightEnWord = enWords.get(fromIndex);
            fromIndex++;
            isRight = rightEnWord != null && rightEnWord.equalsIgnoreCase(enWord);
            if (isRight) {
                fromRight++;
                fromPoints += Settings.POINTS_RIGHT_TRANSLATION;
            } else {
                fromWrong++;
                fromPoints -= Settings.POINTS_ERROR_PENALTY;
            }
        } else if (username.equals(to)) {
            rightEnWord = enWords.get(toIndex);
            isRight = rightEnWord != null && rightEnWord.equalsIgnoreCase(enWord);
            toIndex++;
            if (isRight) {
                toRight++;
                toPoints += Settings.POINTS_RIGHT_TRANSLATION;
            } else {
                toWrong++;
                toPoints -= Settings.POINTS_ERROR_PENALTY;
            }
        } else {
            return;
        }

        if (hasPlayerEnded(from) && hasPlayerEnded(to))
            onChallengeEnded();
    }

    /** Returns true if the given player has ended the challenge, false otherwise. A user has ended the challenge if
     * it has sent all the translations */
    public boolean hasPlayerEnded(String username) {
        if (username.equals(from)) {
            return fromIndex == itWords.size();
        } else if (username.equals(to)) {
            return toIndex == itWords.size();
        }
        return true;
    }

    /** Returns true if the challenge ended because of the timeout or because both users have sent all the translations */
    public boolean isGameEnded() {
        return ended || (hasPlayerEnded(from) && hasPlayerEnded(to));
    }

    /** Returns the points scored by the given user */
    public int getPoints(String username) {
        if (username.equals(from)) {
            return fromPoints;
        } else if (username.equals(to)) {
            return toPoints;
        }
        return 0;
    }

    /** Returns the points scored by opponent of the given user */
    public int getOtherPoints(String username) {
        if (username.equals(from)) {
            return toPoints;
        } else if (username.equals(to)) {
            return fromPoints;
        }
        return 0;
    }

    /** Returns the points scored by the given user. If the user has won, it returns its points plus the extra points */
    public int getFinalPoints(String username) {
        int points = getPoints(username);
        if (hasWon(username)) points += Settings.EXTRA_POINTS;
        return points;
    }

    /** Returns how many words has the given user translated correctly */
    public int getRightCounter(String username) {
        if (username.equals(from)) {
            return fromRight;
        } else if (username.equals(to)) {
            return toRight;
        }
        return 0;
    }

    /** Returns how many words has the given user translated wrongly */
    public int getWrongCounter(String username) {
        if (username.equals(from)) {
            return fromWrong;
        } else if (username.equals(to)) {
            return toWrong;
        }
        return 0;
    }

    /** Invoked when challenge ends */
    public void onChallengeEnded() {
        if (this.ended) return;
        this.ended = true;
    }

    /** Returns true if the given user has won the challenge, false otherwise */
    public boolean hasWon(String username) {
        if (username.equals(from))
            return fromPoints > toPoints;
        else if (username.equals(to))
            return toPoints > fromPoints;
        return false;
    }

    /** Sets the timer that will handle the challenge timeout */
    public void setTimer(TimerTask timer) {
        this.timer = timer;
    }

    /** Cancels the timer that will handle the challenge timeout */
    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
