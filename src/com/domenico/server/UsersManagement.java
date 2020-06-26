package com.domenico.server;

import java.util.List;

public interface UsersManagement {
    void register(User user) throws IllegalArgumentException;
    void login(User user) throws IllegalArgumentException;
    void logout(String username) throws IllegalArgumentException;
    void addFriend(String username, String friendUsername) throws IllegalArgumentException;
    List<String> getFriendList(String username) throws IllegalArgumentException;
}
