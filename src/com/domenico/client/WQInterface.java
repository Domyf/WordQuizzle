package com.domenico.client;

import org.json.simple.JSONArray;

public interface WQInterface {

    boolean onRegisterUser(String username, String password) throws Exception;

    String onLogin(String username, String password) throws Exception;

    String onLogout() throws Exception;

    String onAddFriend(String friendUsername) throws Exception;

    JSONArray onShowFriendList() throws Exception;

    String onSendChallengeRequest(String friendUsername) throws Exception;

    boolean getChallengeResponse(String friendUsername) throws Exception;

    String onShowScore() throws Exception;

    JSONArray onShowLeaderboard() throws Exception;

    void onExit() throws Exception;
}
