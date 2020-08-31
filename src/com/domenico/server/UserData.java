package com.domenico.server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class UserData implements JSONStreamAware {

    private final static String JSON_PASSWORD = "password";
    private final static String JSON_FRIENDS = "friends";
    private final static String JSON_SCORE = "score";

    private final String password;
    private List<String> friends;
    private int score;
    private boolean online; //true if the user is online, false otherwise. It is not saved into file

    public UserData(String password) {
        this.password = password;
        this.friends = new ArrayList<>();
        this.score = 0;
        this.online = false;
    }

    /**
     * Instantiates a UserData object from a JSONObject. It does the opposite of writeJSONString(writer) method
     *
     * @param obj the object to be parsed relative to a UserData object
     * @return a new UserData object
     */
    public static UserData newFromJSON(JSONObject obj) {
        UserData userData = new UserData((String) obj.get(JSON_PASSWORD));
        userData.online = false;
        userData.score = ((Long) obj.get(JSON_SCORE)).intValue();
        userData.friends = (JSONArray) obj.get(JSON_FRIENDS);
        return userData;
    }

    @Override
    public void writeJSONString(Writer writer) throws IOException {
        JSONObject obj = new JSONObject();
        obj.put(JSON_PASSWORD, password);
        obj.put(JSON_SCORE, score);
        obj.put(JSON_FRIENDS, friends);
        obj.writeJSONString(writer);
    }

    /**
     * Compares the user's password with the given password. Thanks to this method, the password is not exposed outside
     * this class.
     *
     * @param password the password to compare to the user's password
     * @return true if the user has the given password, false otherwise
     */
    public boolean hasPassword(String password) {
        return this.password.equals(password);
    }

    public void addFriend(String friendUsername) {
        friends.add(friendUsername);
    }

    public boolean hasFriend(String friendUsername) {
        return friends.contains(friendUsername);
    }

    public String getFriendsJSON() {
        return JSONArray.toJSONString(friends);
    }

    public int getScore() {
        return score;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public List<String> getFriends() { return friends; }

    public String getPassword() {
        return password;
    }
}
