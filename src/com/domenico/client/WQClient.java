package com.domenico.client;

import com.domenico.shared.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WQClient implements WQInterface {

    private static final String ENCRYPTION_ALGORITHM = "SHA-256";   //the algorithm used to encrypt the user's password
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
            String encyptedPassword = Utils.encrypt(password, ENCRYPTION_ALGORITHM);
            return rmiClient.register(username, encyptedPassword);
        } else {
            return Messages.LOGOUT_NEEDED;
        }
    }

    @Override
    public String login(String username, String password) throws Exception {
        if (!isLoggedIn()) {
            String encyptedPassword = Utils.encrypt(password, ENCRYPTION_ALGORITHM);
            String result = tcpClient.login(username, encyptedPassword, udpClient.getUDPPort());
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
    public LinkedHashMap<String, Integer> getLeaderBoard() throws Exception {
        if (isLoggedIn()) {
            JSONObject leaderboard = tcpClient.showLeaderboard(loggedUserName);
            //sort the leaderboard
            List<Map.Entry<String, Long>> list = new ArrayList<>(leaderboard.entrySet());
            list.sort(Map.Entry.comparingByValue());
            //create a new (sorted) linked hash map
            LinkedHashMap<String, Integer> sortedLeaderboard = new LinkedHashMap<>();
            for (int i = list.size() - 1; i >= 0; i--) {
                Map.Entry<String, Long> entry = list.get(i);
                sortedLeaderboard.put(entry.getKey(), entry.getValue().intValue());
            }
            return sortedLeaderboard;
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
