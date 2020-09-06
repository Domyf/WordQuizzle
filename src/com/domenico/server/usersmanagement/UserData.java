package com.domenico.server.usersmanagement;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/** Class that represents a Word Quizzle user. It also has the methods to write or read a user into/from JSON string */
public class UserData implements JSONStreamAware {

    //labels used to represent each field in json form
    private final static String JSON_PASSWORD = "password";
    private final static String JSON_FRIENDS = "friends";
    private final static String JSON_SCORE = "score";

    private final String encryptedPassword;
    private List<String> friends;   //user's friends. Unordered. It contains just the usernames
    private int score;  //user's score
    private boolean online; //true if the user is online, false otherwise. It is not saved into file

    public UserData(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
        this.friends = new ArrayList<>();
        this.score = 0;
        this.online = false;
    }

    /**
     * Instantiates a UserData object from a JSONObject. It does the opposite of writeJSONString(writer) method
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

    /**
     * Writes this into json form.
     * @param writer used to write the JSON string
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void writeJSONString(Writer writer) throws IOException {
        JSONObject obj = new JSONObject();
        obj.put(JSON_PASSWORD, encryptedPassword);
        obj.put(JSON_SCORE, score);
        obj.put(JSON_FRIENDS, friends);
        obj.writeJSONString(writer);
    }

    /**
     * Compares the user's password with the given password. Thanks to this method, the password is not exposed outside
     * this class.
     * @param password the password to compare to the user's password
     * @return true if the user has the given password, false otherwise
     */
    public boolean hasPassword(String password) {
        return this.encryptedPassword.equals(password);
    }

    /** Adds a new friend */
    public void addFriend(String friendUsername) {
        friends.add(friendUsername);
    }

    /** Returns true if this user and the given user are friends */
    public boolean hasFriend(String friendUsername) {
        return friends.contains(friendUsername);
    }

    /** Trasforms this user's friend in JSON form and returns a json string. */
    public String getFriendsJSON() {
        return JSONArray.toJSONString(friends);
    }

    /** Returns a list of this user's friends */
    public List<String> getFriends() { return friends; }

    /** Returns this user's score */
    public int getScore() {
        return score;
    }

    /** Adds the given score to the user score. If the given score is negative, this method will subtract the user's score
     * by the given value. */
    public void addScore(int score) {
        setScore(this.score + score);
    }

    /** Sets the given score to this user's score. It avoids negative score by setting zero instead of a negative value */
    private void setScore(int score) {
        if (score < 0) score = 0;   //avoid negative score
        this.score = score;
    }

    /** Returns true if this user is online, false otherwise */
    public boolean isOnline() {
        return online;
    }

    /** Sets if the user is online or not */
    public void setOnline(boolean online) {
        this.online = online;
    }

    /** Returns this user password (which is encrypted) */
    public String getPassword() {
        return encryptedPassword;
    }
}
