package com.domenico.client;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.LinkedHashMap;

public interface WQInterface {

    String register(String username, String password) throws Exception;

    String login(String username, String password) throws Exception;

    String logout() throws Exception;

    String addFriend(String friendUsername) throws Exception;

    JSONArray getFriendList() throws Exception;

    String sendChallengeRequest(String friendUsername) throws Exception;

    boolean waitChallengeResponse(StringBuffer response) throws Exception;

    void sendChallengeResponse(boolean accepted) throws Exception;

    String onChallengeStart() throws Exception;

    String giveTranslation(String enWord) throws Exception;

    String getScore() throws Exception;

    LinkedHashMap<String, Integer> getLeaderBoard() throws Exception;

    /**
     * @return true if the client's user is logged in, false otherwise
     */
    boolean isLoggedIn();

    void exit() throws Exception;

    int getChallengeLength();

    int getChallengeWords();

    boolean isPlaying();

    int getWordCounter();

    void waitChallengeEnd() throws Exception;
}
