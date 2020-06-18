package com.domenico.communication;

import java.util.Arrays;

public class ConnectionData {
    private enum CMD {
        LOGIN_REQUEST,
        LOGOUT_REQUEST,
        ADD_FRIEND_REQUEST,
        FRIEND_LIST_REQUEST,
        SCORE_REQUEST,
        LEADERBOARD_REQUEST,
        SUCCESS_RESPONSE,
        FAIL_RESPONSE
    }

    private CMD cmd;
    private String[] params;

    private ConnectionData(CMD cmd, String[] params) {
        this.cmd = cmd;
        this.params = params;
    }

    public String getParam(int i) {
        return params[i];
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            builder.append(params[i]).append(" ");
        }
        return cmd.toString()+" "+builder.toString();
    }

    public static ConnectionData parseLine(String line) {
        String[] splittedLine = line.split(" ");
        String cmd;
        if (splittedLine.length > 0) {
            String[] params = new String[0];
            cmd = splittedLine[0];
            if (splittedLine.length > 1)
                params = Arrays.copyOfRange(splittedLine, 1, splittedLine.length);
            return new ConnectionData(CMD.valueOf(cmd), params);
        }
        return null;
    }

    public static class Factory {
        public static ConnectionData newLoginRequest(String username, String password) {
            return new ConnectionData(CMD.LOGIN_REQUEST, new String[]{username, password});
        }

        public static ConnectionData newLogoutRequest(String username) {
            return new ConnectionData(CMD.LOGOUT_REQUEST, new String[]{username});
        }

        public static ConnectionData newAddFriendRequest(String username, String friendUsername) {
            return new ConnectionData(CMD.ADD_FRIEND_REQUEST, new String[]{username, friendUsername});
        }

        public static ConnectionData newFriendListRequest(String username) {
            return new ConnectionData(CMD.FRIEND_LIST_REQUEST, new String[]{username});
        }

        public static ConnectionData newScoreRequest(String username) {
            return new ConnectionData(CMD.SCORE_REQUEST, new String[]{username});
        }

        public static ConnectionData newLeaderboardRequest(String username) {
            return new ConnectionData(CMD.LEADERBOARD_REQUEST, new String[]{username});
        }

        public static ConnectionData newSuccessResponse() {
            return new ConnectionData(CMD.SUCCESS_RESPONSE, new String[]{});
        }

        public static ConnectionData newFailResponse(String failmessage) {
            return new ConnectionData(CMD.FAIL_RESPONSE, new String[]{failmessage});
        }
    }

    public static class Validator {
        private static boolean hasSameCMD(CMD first, CMD second) {
            return first.compareTo(second) == 0;
        }

        public static boolean isLoginRequest(ConnectionData request) {
            return hasSameCMD(CMD.LOGIN_REQUEST, request.cmd);
        }

        public static boolean isLogoutRequest(ConnectionData request) {
            return hasSameCMD(CMD.LOGOUT_REQUEST, request.cmd);
        }

        public static boolean isAddFriendRequest(ConnectionData request) {
            return hasSameCMD(CMD.ADD_FRIEND_REQUEST, request.cmd);
        }

        public static boolean isFriendListRequest(ConnectionData request) {
            return hasSameCMD(CMD.FRIEND_LIST_REQUEST, request.cmd);
        }

        public static boolean isScoreRequest(ConnectionData request) {
            return hasSameCMD(CMD.SCORE_REQUEST, request.cmd);
        }

        public static boolean isLeaderboardRequest(ConnectionData request) {
            return hasSameCMD(CMD.LEADERBOARD_REQUEST, request.cmd);
        }

        public static boolean isSuccessResponse(ConnectionData request) {
            return hasSameCMD(CMD.SUCCESS_RESPONSE, request.cmd);
        }

        public static boolean isFailResponse(ConnectionData request) {
            return hasSameCMD(CMD.FAIL_RESPONSE, request.cmd);
        }
    }
}
