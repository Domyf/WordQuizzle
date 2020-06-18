package com.domenico.server;

public interface UsersManagement {
    void register(User user);
    boolean isRegistered(String username);
    void login(User user);
    boolean isLoggedIn(String username);
    void logout(String username);
}
