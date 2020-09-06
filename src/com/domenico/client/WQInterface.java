package com.domenico.client;

import org.json.simple.JSONArray;

import java.util.LinkedHashMap;

public interface WQInterface {

    /**
     * Registers a new user with the given username and password.
     * @param username user's username
     * @param password user's password
     * @return an empty string in case of success or an error message in case of failure
     * @throws Exception if an error occurs while sending the message or while receiving the response
     */
    String register(String username, String password) throws Exception;

    /**
     * Login the user with the given username and password.
     *
     * @param username user's username
     * @param password user's password. It will be encrypted before being sent
     * @return an empty string in case of success or an error message in case of failure
     * @throws Exception
     */
    String login(String username, String password) throws Exception;

    String logout() throws Exception;

    String addFriend(String friendUsername) throws Exception;

    JSONArray getFriendList() throws Exception;

    String sendChallengeRequest(String friendUsername) throws Exception;

    boolean waitChallengeResponse(StringBuffer response) throws Exception;

    void sendChallengeResponse(boolean accepted) throws Exception;

    String onChallengeStart() throws Exception;

    String giveTranslation(String enWord) throws Exception;

    int getScore(StringBuffer failMessage) throws Exception;

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
