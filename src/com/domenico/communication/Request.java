package com.domenico.communication;

import java.util.Arrays;

public class Request {
    private enum CMD {
        LOGIN_REQUEST,
        LOGOUT_REQUEST,
        ADD_FRIEND_REQUEST,
        FRIEND_LIST_REQUEST,
        SCORE_REQUEST,
    }

    private CMD cmd;
    private String[] params;

    private Request(CMD cmd, String[] params) {
        this.cmd = cmd;
        this.params = params;
    }

    @Override
    public String toString() {
        return cmd+" "+Arrays.toString(params);
    }

    public static class RequestFactory {
        public static Request newLoginRequest(String username, String password) {
            return new Request(CMD.LOGIN_REQUEST, new String[]{username, password});
        }

        public static Request newLogoutRequest(String username) {
            return new Request(CMD.LOGOUT_REQUEST, new String[]{username});
        }

        public static Request newAddFriendRequest(String username) {
            return new Request(CMD.ADD_FRIEND_REQUEST, new String[]{username});
        }

        public static Request newFriendListRequest(String username) {
            return new Request(CMD.FRIEND_LIST_REQUEST, new String[]{username});
        }

        public static Request newScoreRequest(String username) {
            return new Request(CMD.SCORE_REQUEST, new String[]{username});
        }
    }

    public static Request parseRequest(String line) {
        String[] splittedLine = line.split(" ");
        CMD cmd = CMD.valueOf(splittedLine[0]);
        String[] params = new String[0];
        if (splittedLine.length > 1)
            params = Arrays.copyOfRange(splittedLine, 1, splittedLine.length);

        return new Request(cmd, params);
    }

    public static boolean isLoginRequest(Request request) {
        return request.cmd.compareTo(CMD.LOGIN_REQUEST) == 0;
    }

    public static boolean isLogoutRequest(Request request) {
        return request.cmd.compareTo(CMD.LOGOUT_REQUEST) == 0;
    }

    public static boolean isAddFriendRequest(Request request) {
        return request.cmd.compareTo(CMD.ADD_FRIEND_REQUEST) == 0;
    }

    public static boolean isScoreRequest(Request request) {
        return request.cmd.compareTo(CMD.SCORE_REQUEST) == 0;
    }

    /*LOGIN_REQUEST,
    LOGOUT_REQUEST,
    ADD_FRIEND_REQUEST,
    FRIEND_LIST_REQUEST,
    SCORE_REQUEST,
    LEADERBOARD_REQUEST,
    SUCCESS,
    FAIL*/
}
