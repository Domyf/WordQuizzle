package com.domenico.client;

import org.json.simple.JSONArray;

import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeoutException;

/** Interface with all the word quizzle functionalities that should be implemented to fully run a Word Quizzle client */
public interface WQInterface {
    /**
     * Registers a new user with the given username and password.
     * @param username user's username
     * @param password user's password
     * @return an empty string in case of success or an error message in case of failure
     * @throws Exception if an error occurs during the registration phase
     */
    String register(String username, String password) throws Exception;

    /**
     * Login the user with the given username and password.
     * @param username user's username
     * @param password user's password. It will be encrypted before being sent
     * @return an empty string in case of success or an error message in case of failure
     * @throws TimeoutException if the server doesn't reply to the request
     * @throws NoSuchAlgorithmException if the client is not able to encrypt the password
     */
    String login(String username, String password) throws TimeoutException, NoSuchAlgorithmException;

    /**
     * It does the logout functionality.
     * @return an empty string in case of success, an error string if something went wrong null if the response from
     * the server is invalid
     * @throws TimeoutException if the server doesn't reply to the request
     */
    String logout() throws TimeoutException;

    /**
     * Add the friend specified to your friend list
     * @param friendUsername friend's username
     * @return an empty string in case of success, an error string if something went wrong null if the response from
     * the server is invalid
     * @throws TimeoutException if the server doesn't reply to the request
     */
    String addFriend(String friendUsername) throws TimeoutException;

    /**
     * Get the user friend list.
     * @return a JSONArray that contains the usernames of all the user's friends or null in case of a bad response from
     * the server
     * @throws TimeoutException if the server doesn't reply to the request
     */
    JSONArray getFriendList() throws TimeoutException;

    /**
     * Send a challenge request to the user's friend. Returns if the challenge has been forwarded or not.
     * @param friendUsername friend's username
     * @return an empty string in case of success, an error string if something went wrong, null if the response from
     * the server is invalid
     * @throws TimeoutException if the server doesn't reply to the request
     */
    String sendChallengeRequest(String friendUsername) throws TimeoutException;

    /**
     * Waits until the user's friend sends a challenge response or until the challenge times out.
     * @param response the challenge response arrived
     * @return true if the challenge was accepted, false otherwise
     * @throws TimeoutException if the server doesn't reply to the request
     */
    boolean waitChallengeResponse(StringBuffer response) throws TimeoutException;

    /**
     * Reply to a challenge request by accepting it or not accepting it
     * @param accepted true if the user wants to accept the challenge, false otherwise
     * @throws TimeoutException if the server doesn't reply to the request
     */
    void sendChallengeResponse(boolean accepted) throws TimeoutException;

    /**
     * To be called when the challenge starts. Returns the first word that should be translated by the user.
     * @return the first word that should be translated by the user or an empty string in case of error
     * @throws TimeoutException if the server doesn't reply to the request
     */
    String onChallengeStart() throws TimeoutException;

    /**
     * Give the translation to the current word and returns the next italian word or an empty string in case the user
     * translated all the challenge's words.
     * @param enWord english word written by the user
     * @return the next word or an empty string in case the user translated all the challenge's words.
     * @throws Exception if something went wrong while waiting the next italian word from the server
     */
    String giveTranslation(String enWord) throws Exception;

    /** Wait until the challenge times out or the next user sends the last translation
     * @throws Exception if something went wrong while waiting the challenge end
     * */
    void waitChallengeEnd() throws Exception;

    /**
     * Get the user's score.
     * @param failMessage if an error occurs, the fail message will be appended to this StringBuffer
     * @return the user's score or -1 in case of error
     * @throws TimeoutException if the server doesn't reply to the request
     */
    int getScore(StringBuffer failMessage) throws TimeoutException;

    /**
     * Get the leaderboard. The iteration order is from the first position to the last one.
     * @return a linked hash map that represents the leaderboard. It maps the username to the relative score. The
     * iteration order is from the best user to the worst.
     * @throws TimeoutException if the server doesn't reply to the request
     */
    LinkedHashMap<String, Integer> getLeaderBoard() throws TimeoutException;

    /** @return true if the client's user is logged in, false otherwise */
    boolean isLoggedIn();

    /** Returns true if the user is playing, false otherwise */
    boolean isPlaying();

    /** Returns how much long (in millisecond) is the challenge */
    int getChallengeLength();

    /** Returns how many italian words should be translated by the user */
    int getChallengeWords();

    /** Returns how many words has the user translated */
    int getWordCounter();

    /** Handles the Word Quizzle exit. Called when the Word Quizzle client should be stopped */
    void exit() throws Exception;
}
