package com.domenico.client;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.rmi.NotBoundException;

public class WQClient implements WQInterface {

    private final RMIClient rmiClient;
    private final TCPClient tcpClient;
    private final UDPClient udpClient;
    private final ChallengeListener challengeListener;  //listener used to signal the events relative to the challenge request
    private String loggedUserName;
    private int challengeLengthSec;
    private int challengeWords;
    private boolean playing;
    private int wordCounter;
    private final Object mutex = new Object();
    private String nextWord;

    public WQClient(ChallengeListener listener) throws IOException, NotBoundException {
        this.loggedUserName = null;
        this.rmiClient = new RMIClient();
        this.tcpClient = new TCPClient(this);
        this.udpClient = new UDPClient(this);
        this.challengeListener = listener;
        new Thread(this.udpClient).start();
    }

    @Override
    public String register(String username, String password) throws Exception {
        if (!isLoggedIn()) {
            return rmiClient.register(username, password);
        } else {
            return Messages.LOGOUT_NEEDED;
        }
    }

    @Override
    public String login(String username, String password) throws Exception {
        if (!isLoggedIn()) {
            String result = tcpClient.login(username, password, udpClient.getUDPPort());
            if (result != null && result.isEmpty()) {
                loggedUserName = username;
                return Messages.LOGIN_SUCCESS;
            }
            return result;
        } else {
            return Messages.LOGOUT_NEEDED;
        }
    }

    @Override
    public String logout() throws Exception {
        if (isLoggedIn()) {
            String result = tcpClient.logout(loggedUserName);
            if (result != null && result.isEmpty()) {
                loggedUserName = null;
                return Messages.LOGOUT_SUCCESS;
            } else {
                return result;
            }
        } else {
            return Messages.LOGIN_NEEDED;
        }
    }

    @Override
    public String addFriend(String friendUsername) throws Exception {
        if (isLoggedIn()) {
            return tcpClient.addFriend(loggedUserName, friendUsername);
        } else {
            return Messages.LOGIN_NEEDED;
        }
    }

    @Override
    public JSONArray getFriendList() throws Exception {
        if (isLoggedIn()) {
            return tcpClient.friendList(loggedUserName);
        } else {
            return null;
        }
    }

    @Override
    public String getScore() throws Exception {
        if (isLoggedIn()) {
            return tcpClient.showScore(loggedUserName);
        } else {
            return Messages.LOGIN_NEEDED;
        }
    }

    @Override
    public JSONObject getLeaderBoard() throws Exception {
        if (isLoggedIn()) {
            return tcpClient.showLeaderboard(loggedUserName);
        } else {
            return null;
        }
    }

    @Override
    public String sendChallengeRequest(String friendUsername) throws Exception {
        if (isLoggedIn()) {
            return tcpClient.sendChallengeRequest(loggedUserName, friendUsername);
        } else {
            return Messages.LOGIN_NEEDED;
        }
    }

    @Override
    public boolean waitChallengeResponse(StringBuffer response) throws Exception {
        playing = tcpClient.getChallengeResponse(response);
        return playing;
    }

    @Override
    public void sendChallengeResponse(boolean accepted) throws Exception {
        udpClient.onChallengeResponse(accepted);
        this.playing = accepted;
    }

    @Override
    public String onChallengeStart() throws Exception {
        String[] challengeSettings = new String[2];
        String word = tcpClient.onChallengeStart(challengeSettings);
        challengeLengthSec = Integer.parseUnsignedInt(challengeSettings[0]) / 1000;
        challengeWords = Integer.parseUnsignedInt(challengeSettings[1]);
        playing = true;
        wordCounter = 1;
        nextWord = null;
        return word;
    }

    @Override
    public String giveTranslation(String enWord) throws Exception {
        tcpClient.sendTranslation(enWord);
        if (wordCounter == challengeWords)
            return "";
        String next;
        synchronized (mutex) {
            while (nextWord == null) {
                mutex.wait();
            }
            next = nextWord;
            nextWord = null;
            wordCounter++;
        }
        return next;
    }

    @Override
    public void exit() {
        synchronized (mutex) {
            mutex.notifyAll();
        }
        this.udpClient.stopProcessing();
        this.tcpClient.exit();
    }

    @Override
    public int getChallengeLength() { return challengeLengthSec; }

    @Override
    public int getChallengeWords() { return challengeWords; }

    @Override
    public boolean isLoggedIn() { return loggedUserName != null; }

    @Override
    public boolean isPlaying() { return playing; }

    @Override
    public int getWordCounter() { return wordCounter; }

    @Override
    public void waitChallengeEnd() throws Exception {
        synchronized (mutex) {
            while (playing) {
                mutex.wait();
            }
        }
    }

    public void onChallengeEnd(int correct, int wrong, int notransl, int yourscore, int otherscore, int extrapoints) {
        this.wordCounter = 1;
        this.playing = false;
        setNextWord("");
        challengeListener.onChallengeEnd(correct, wrong, notransl, yourscore, otherscore, extrapoints);
    }

    public void onChallengeArrived(String from) {
        challengeListener.onChallengeArrived(from);
    }

    public void onChallengeArrivedTimeout() {
        this.playing = false;
        challengeListener.onChallengeRequestTimeout();
    }

    public void setNextWord(String nextWord) {
        synchronized (mutex) {
            this.nextWord = nextWord;
            mutex.notify();
        }
    }
}
