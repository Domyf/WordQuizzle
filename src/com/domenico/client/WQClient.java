package com.domenico.client;

import com.domenico.shared.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/** Implementation of the WordQuizzle backend. It runs the RMI service and two threads which handles the UDP and TCP
 * communications */
public class WQClient implements WQInterface, ChallengeListener {

    public static final int SERVER_RESPONSE_TIMEOUT = 20000; //max waiting time for a server response (in milliseconds)
    private static final String ENCRYPTION_ALGORITHM = "SHA-256";   //the algorithm used to encrypt the user's password

    private final RMIClient rmiClient;
    private final TCPClient tcpClient;
    private final UDPClient udpClient;
    //listener used to signal the events relative to the challenge request
    private final ChallengeListener challengeListener;
    private String loggedUserName = null;  //logged user's username or null if the user is not logged
    private int challengeLengthSec; //how many seconds the challenge is long
    private int challengeWords; //how many words the user should translate in the challenge
    private boolean playing;    //true if the user is playing a challenge, false otherwise
    private int wordCounter;    //how many translations has the user sent
    private final Object mutex = new Object();
    private String nextWord;

    public WQClient(ChallengeListener listener) throws IOException, NotBoundException {
        this.rmiClient = new RMIClient();
        this.tcpClient = new TCPClient(this);
        this.udpClient = new UDPClient(this);
        this.challengeListener = listener;
        new Thread(this.udpClient).start();
    }

    @Override
    public String register(String username, String password) throws Exception {
        if (!isLoggedIn()) {
            //encrypt the password
            String encyptedPassword = Utils.encrypt(password, ENCRYPTION_ALGORITHM);
            //register the user by its username and its encrypted password
            return rmiClient.register(username, encyptedPassword);
        } else {
            return Messages.LOGOUT_NEEDED;
        }
    }

    @Override
    public String login(String username, String password) throws TimeoutException, NoSuchAlgorithmException {
        if (!isLoggedIn()) {
            String encyptedPassword = Utils.encrypt(password, ENCRYPTION_ALGORITHM);
            String failureMessage = tcpClient.login(username, encyptedPassword, udpClient.getUDPPort());
            if (failureMessage != null && failureMessage.isEmpty()) {
                loggedUserName = username;
                return "";
            }
            return failureMessage;
        } else {
            return Messages.LOGOUT_NEEDED;
        }
    }

    @Override
    public String logout() throws TimeoutException {
        if (isLoggedIn()) {
            String result = tcpClient.logout(loggedUserName);
            if (result != null && result.isEmpty()) {
                loggedUserName = null;
                return "";
            } else {
                return result;
            }
        } else {
            return Messages.LOGIN_NEEDED;
        }
    }

    @Override
    public String addFriend(String friendUsername) throws TimeoutException {
        if (isLoggedIn()) {
            return tcpClient.addFriend(loggedUserName, friendUsername);
        } else {
            return Messages.LOGIN_NEEDED;
        }
    }

    @Override
    public JSONArray getFriendList() throws TimeoutException {
        if (isLoggedIn()) {
            return tcpClient.friendList(loggedUserName);
        } else {
            return null;
        }
    }

    @Override
    public int getScore(StringBuffer failMessage) throws TimeoutException {
        if (isLoggedIn()) {
            return tcpClient.showScore(loggedUserName);
        } else {
            failMessage.append(Messages.LOGIN_NEEDED);
            return -1;
        }
    }

    @Override
    public LinkedHashMap<String, Integer> getLeaderBoard() throws TimeoutException {
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
    public String sendChallengeRequest(String friendUsername) throws TimeoutException {
        if (isLoggedIn()) {
            return tcpClient.sendChallengeRequest(loggedUserName, friendUsername);
        } else {
            return Messages.LOGIN_NEEDED;
        }
    }

    @Override
    public boolean waitChallengeResponse(StringBuffer response) throws TimeoutException {
        playing = tcpClient.getChallengeResponse(response);
        return playing;
    }

    @Override
    public void sendChallengeResponse(boolean accepted) {
        udpClient.onChallengeResponse(accepted);
        this.playing = accepted;
    }

    @Override
    public String onChallengeStart() throws TimeoutException {
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

    @Override
    public void onChallengeEnd(int correct, int wrong, int notransl, int yourscore, int otherscore, int extrapoints) {
        this.wordCounter = 1;
        this.playing = false;
        setNextWord("");
        challengeListener.onChallengeEnd(correct, wrong, notransl, yourscore, otherscore, extrapoints);
    }

    @Override
    public void onChallengeArrived(String from) {
        challengeListener.onChallengeArrived(from);
    }

    @Override
    public void onChallengeRequestTimeout() {
        this.playing = false;
        challengeListener.onChallengeRequestTimeout();
    }

    /** Sets the next word with the one specified */
    public void setNextWord(String nextWord) {
        synchronized (mutex) {
            this.nextWord = nextWord;
            mutex.notify();
        }
    }
}
