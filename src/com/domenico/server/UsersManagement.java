package com.domenico.server;

import org.json.simple.JSONObject;

import java.io.*;
import java.util.*;

//TODO write documentation for this class
public class UsersManagement {

    private static UsersManagement instance;
    private final HashMap<String, UserData> serverData; //map username -> user data

    private UsersManagement() throws IOException {
        this.serverData = (HashMap<String, UserData>) Persistence.readFromDisk();
    }

    public static UsersManagement getInstance() throws IOException {
        if (instance == null)
            instance = new UsersManagement();
        return instance;
    }

    public void register(String username, String password) throws UsersManagementException {
        if (serverData.get(username) != null)
            throw new UsersManagementException("Non è possibile registrarsi: questo nome utente è già utilizzato");

        serverData.put(username, new UserData(password));
        Persistence.saveOnDisk(serverData);
    }

    public void login(String username, String password) throws UsersManagementException {
        UserData userData = serverData.get(username);
        if (userData == null || !userData.hasPassword(password))
            throw new UsersManagementException("Username o password non valida");
        if (userData.isOnline())
            throw new UsersManagementException("Hai già eseguito il login");

        userData.setOnline(true);
    }

    public void logout(String username) throws UsersManagementException {
        UserData userData = serverData.get(username);
        if (userData == null)
            throw new UsersManagementException("Username non valida");
        if (!userData.isOnline())
            throw new UsersManagementException("Hai già eseguito il logout");

        userData.setOnline(false);
    }

    public void addFriend(String username, String friendUsername) throws UsersManagementException {
        if (username.equals(friendUsername))
            throw new UsersManagementException("Non puoi diventare amico con te stesso");
        UserData first = serverData.get(username);
        UserData second = serverData.get(friendUsername);
        if (first == null)
            throw new UsersManagementException("Username non valida");
        if (second == null)
            throw new UsersManagementException("Non esiste un altro utente con questa username");

        if (areFriends(username, friendUsername)) {
            throw new UsersManagementException("L'amicizia non è stata creata perchè siete già amici");
        } else {
            first.addFriend(friendUsername);
            second.addFriend(username);
            Persistence.saveOnDisk(serverData);
        }
    }

    public int getScore(String username) throws UsersManagementException {
        UserData userData = serverData.get(username);
        if (userData == null)
            throw new UsersManagementException("Username non valida");
        return userData.getScore();
    }

    public void addScore(String username, int newScore) throws UsersManagementException {
        UserData userData = serverData.get(username);
        if (userData == null)
            throw new UsersManagementException("Username non valida");

        userData.addScore(newScore);
        Persistence.saveOnDisk(serverData);
    }

    public boolean isOnline(String username) {
        UserData userData = serverData.get(username);
        return userData != null && userData.isOnline();
    }

    public boolean areFriends(String firstUsername, String secondUsername) {
        UserData firstData = serverData.get(firstUsername);
        if (firstData != null)
            return firstData.hasFriend(secondUsername);
        return false;
    }

    public String getJSONFriendList(String username) throws UsersManagementException {
        UserData userData = serverData.get(username);
        if (userData == null)
            throw new UsersManagementException("Username non valida");
        return userData.getFriendsJSON();
    }

    public JSONObject getLeaderboard(String username) throws UsersManagementException {
        UserData userData = serverData.get(username);
        if (userData == null)
            throw new UsersManagementException("Username non valida");

        JSONObject obj = new JSONObject();
        obj.put(username, userData.getScore());
        for (String friend:userData.getFriends()) {
            obj.put(friend, serverData.get(friend).getScore());
        }
        return obj;
    }
}
