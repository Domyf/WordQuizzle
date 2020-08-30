package com.domenico.server;

import com.domenico.communication.ConnectionData;

import java.net.InetAddress;
import java.nio.channels.SelectionKey;

public interface WQHandler {

    ConnectionData handleLoginRequest(ConnectionData received, SelectionKey key, InetAddress inetAddress) throws UsersManagementException;

    ConnectionData handleLogoutRequest(ConnectionData received) throws UsersManagementException;

    ConnectionData handleAddFriendRequest(ConnectionData received) throws UsersManagementException;

    ConnectionData handleFriendListRequest(ConnectionData received) throws UsersManagementException;

    ConnectionData handleChallengeRequest(ConnectionData received, SelectionKey key) throws UsersManagementException;

    void handleChallengeResponse(Challenge challenge, SelectionKey fromKey, SelectionKey toKey);

    void handleChallengeWordsReady(Challenge challenge, SelectionKey fromKey, SelectionKey toKey);

    ConnectionData handleScoreRequest(ConnectionData received) throws UsersManagementException;

    ConnectionData handleLeaderboardRequest(ConnectionData received) throws UsersManagementException;

    void handleUserDisconnected(SelectionKey key);
}
