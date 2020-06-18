package com.domenico.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersManager implements UsersManagement {

    private static UsersManager instance;
    private Map<User, List<String>> users;  //map user -> its friend's usernames

    private UsersManager() {
        this.users = new HashMap<>();
    }

    public static UsersManager getInstance() {
        if (instance == null)
            instance = new UsersManager();
        return instance;
    }

    @Override
    public void register(User user) {
        if (!users.containsKey(user))
            users.put(user, new ArrayList<>());
    }

    @Override
    public boolean isRegistered(String username) {
        User found = findByUsername(username);
        return found != null;
    }

    @Override
    public void login(User user) {
        if (users.containsKey(user)) {
            //then username and password are the same
        }
        notImplemented();
    }

    @Override
    public boolean isLoggedIn(String username) {
        return false;
    }

    @Override
    public void logout(String username) {
        notImplemented();
    }

    private User findByUsername(String username) {
        for (User user :users.keySet()) {
            if (user.getUsername().equals(username))
                return user;
        }
        return null;
    }

    private void notImplemented() {
        System.out.println("Not implemented yet");
    }
}
