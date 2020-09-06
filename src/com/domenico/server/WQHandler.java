package com.domenico.server;

import com.domenico.communication.ConnectionData;

import java.net.InetAddress;
import java.nio.channels.SelectionKey;

/**
 * Handles all the WordQuizzle request arrived via the network and for some cases prepares the response to send back.
 */
public interface WQHandler {

    /**
     * Invoked when a login request has arrived.
     * @param received the message arrived
     * @param key who has sent the request
     * @param inetAddress the remote address of the sender
     * @return the response to send back
     * @throws UsersManagementException if it's not possible to do the login
     */
    ConnectionData handleLoginRequest(ConnectionData received, SelectionKey key, InetAddress inetAddress) throws UsersManagementException;

    /**
     * Invoked when a logout request has arrived.
     * @param received the message arrived
     * @return the response to send back
     * @throws UsersManagementException if it's not possible to do the logout
     */
    ConnectionData handleLogoutRequest(ConnectionData received) throws UsersManagementException;

    /**
     * Invoked when an add friend request has arrived.
     * @param received the message arrived
     * @return the response to send back
     * @throws UsersManagementException if it's not possible to add the friend
     */
    ConnectionData handleAddFriendRequest(ConnectionData received) throws UsersManagementException;

    /**
     * Invoked when a friend list request has arrived.
     * @param received the message arrived
     * @return the response to send back
     * @throws UsersManagementException if it's not possible to do get the friend list
     */
    ConnectionData handleFriendListRequest(ConnectionData received) throws UsersManagementException;

    /**
     * Invoked when a challenge request has arrived.
     * @param received the message arrived
     * @param key who has sent the request
     * @return the response to send back
     * @throws UsersManagementException if it's not possible forward the challenge
     */
    ConnectionData handleChallengeRequest(ConnectionData received, SelectionKey key) throws UsersManagementException;

    /**
     * Invoked when a score request has arrived.
     * @param received the message arrived
     * @return the response to send back
     * @throws UsersManagementException if it's not possible to get the user's score
     */
    ConnectionData handleScoreRequest(ConnectionData received) throws UsersManagementException;

    /**
     * Invoked when a leaderboard request has arrived.
     * @param received the message arrived
     * @return the response to send back
     * @throws UsersManagementException if it's not possible to get the leaderboard
     */
    ConnectionData handleLeaderboardRequest(ConnectionData received) throws UsersManagementException;

    /**
     * Invoked when a translation has arrived.
     * @param received the message arrived
     * @param key who has sent the request
     * @return the response to send back
     */
    ConnectionData handleTranslationArrived(ConnectionData received, SelectionKey key);

    /**
     * Invoked when a challenge response has arrived.
     * @param challenge the challenge between the two users
     * @param fromKey who has sent the request
     * @param toKey who has received the request
     */
    void handleChallengeResponse(Challenge challenge, SelectionKey fromKey, SelectionKey toKey);

    /**
     * Invoked when the translation of the challenge's words is available.
     * @param challenge the challenge between the two users
     * @param fromKey who has sent the request
     * @param toKey who has received the request
     */
    void handleChallengeWordsReady(Challenge challenge, SelectionKey fromKey, SelectionKey toKey);

    /**
     * Invoked when the user disconnects.
     * @param key user's key
     */
    void handleUserDisconnected(SelectionKey key);
}
