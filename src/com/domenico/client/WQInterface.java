package com.domenico.client;

import org.json.simple.JSONArray;

public interface WQInterface {

    String onRegisterUser(String username, String password) throws Exception;

    String onLogin(String username, String password) throws Exception;

    String onLogout() throws Exception;

    String onAddFriend(String friendUsername) throws Exception;

    JSONArray onShowFriendList() throws Exception;

    String onSendChallengeRequest(String friendUsername) throws Exception;

    boolean getChallengeResponse(String friendUsername) throws Exception;

    String onShowScore() throws Exception;

    JSONArray onShowLeaderboard() throws Exception;

    /**
     * @return true if the client's user is logged in
     */
    boolean isLoggedIn();

    void onExit() throws Exception;

}
