package com.domenico.communication;

public class LoginRequest extends Request {

    public static final String COMMAND = "LOGIN_REQUEST";

    private String username;
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public LoginRequest(String[] line) {
        this.username = line[1];
        this.password = line[2];
    }

    @Override
    String toLine() {
        return COMMAND+" "+username+" "+password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
