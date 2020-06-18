package com.domenico.server;

public class User {

    private String username;
    private String password;
    private boolean loggedIn;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    /**
     * Compares this to the given obj. If a User object is passed then the comparison only takes in account the username
     * and the password.
     * @param obj the object that is compared with this
     * @return true if this and obj have the same username and password, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            User other = (User) obj;
            return other.username.equals(this.username) && other.password.equals(this.password);
        }
        return super.equals(obj);
    }
}
