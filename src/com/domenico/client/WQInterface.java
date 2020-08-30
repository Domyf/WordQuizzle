package com.domenico.client;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;

public interface WQInterface {

    String onRegisterUser(String username, String password) throws Exception;

    String onLogin(String username, String password) throws Exception;

    String onLogout() throws Exception;

    String onAddFriend(String friendUsername) throws Exception;

    JSONArray onShowFriendList() throws Exception;

    String onSendChallengeRequest(String friendUsername) throws Exception;

    boolean getChallengeResponse(StringBuffer response) throws Exception;

    String onChallengeStart() throws IOException;

    String getNextWord() throws IOException;

    boolean sendTranslation(String enWord);

    String onShowScore() throws Exception;

    JSONObject onShowLeaderboard() throws Exception;

    /**
     * @return true if the client's user is logged in, false otherwise
     */
    boolean isLoggedIn();

    void onExit() throws Exception;

    int getChallengeLength();

    int getChallengeWords();
}
