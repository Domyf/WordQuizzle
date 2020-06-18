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
    private String senderUsername;
    private String senderPassword;
    private String friendUsername;
    private String message;

    private ConnectionData(CMD cmd, String[] params) {
        this.cmd = cmd;
        this.params = params;
        senderUsername = null;
        senderPassword = null;
        friendUsername = null;
        message = null;
    }

    public String getUsername() {
        return senderUsername;
    }

    public String getPassword() {
        return senderPassword;
    }

    public String getFriendUsername() {
        return friendUsername;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            builder.append(params[i]).append(" ");
        }
        return cmd.toString()+" "+builder.toString();
    }

    /*public static ConnectionData parseLine(String line) {
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
    }*/

    public static class Factory {
        public static ConnectionData parseLine(String line) {
            String[] splittedLine = line.split(" ");
            String cmd;
            if (splittedLine.length > 0) {
                String[] params = new String[0];
                cmd = splittedLine[0];
                if (splittedLine.length > 1)
                    params = Arrays.copyOfRange(splittedLine, 1, splittedLine.length);
                switch (CMD.valueOf(cmd)) {
                    case LOGIN_REQUEST:
                        return newLoginRequest(params[0], params[1]);
                    case LOGOUT_REQUEST:
                        return newLogoutRequest(params[0]);
                    case ADD_FRIEND_REQUEST:
                        return newAddFriendRequest(params[0], params[1]);
                    case FRIEND_LIST_REQUEST:
                        return newFriendListRequest(params[0]);
                    case SCORE_REQUEST:
                        return newScoreRequest(params[0]);
                    case LEADERBOARD_REQUEST:
                        return newLeaderboardRequest(params[0]);
                    case SUCCESS_RESPONSE:
                        return newSuccessResponse();
                    case FAIL_RESPONSE:
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < params.length; i++) {
                            builder.append(params[i]).append(" ");
                        }
                        return newFailResponse(builder.toString());
                }
            }
            return null;
        }

        public static ConnectionData newLoginRequest(String username, String password) {
            ConnectionData connectionData = new ConnectionData(CMD.LOGIN_REQUEST, new String[]{username, password});
            connectionData.senderUsername = username;
            connectionData.senderPassword = password;
            return connectionData;
        }

        public static ConnectionData newLogoutRequest(String username) {
            ConnectionData connectionData = new ConnectionData(CMD.LOGOUT_REQUEST, new String[]{username});
            connectionData.senderUsername = username;
            return connectionData;
        }

        public static ConnectionData newAddFriendRequest(String username, String friendUsername) {
            ConnectionData connectionData = new ConnectionData(CMD.ADD_FRIEND_REQUEST, new String[]{username, friendUsername});
            connectionData.senderUsername = username;
            connectionData.friendUsername = friendUsername;
            return connectionData;
        }

        public static ConnectionData newFriendListRequest(String username) {
            ConnectionData connectionData = new ConnectionData(CMD.FRIEND_LIST_REQUEST, new String[]{username});
            connectionData.senderUsername = username;
            return connectionData;
        }

        public static ConnectionData newScoreRequest(String username) {
            ConnectionData connectionData = new ConnectionData(CMD.SCORE_REQUEST, new String[]{username});
            connectionData.senderUsername = username;
            return connectionData;
        }

        public static ConnectionData newLeaderboardRequest(String username) {
            ConnectionData connectionData = new ConnectionData(CMD.LEADERBOARD_REQUEST, new String[]{username});
            connectionData.senderUsername = username;
            return connectionData;
        }

        public static ConnectionData newSuccessResponse() {
            return new ConnectionData(CMD.SUCCESS_RESPONSE, new String[]{});
        }

        public static ConnectionData newFailResponse(String failmessage) {
            ConnectionData connectionData = new ConnectionData(CMD.FAIL_RESPONSE, new String[]{failmessage});
            connectionData.message = failmessage;
            return connectionData;
        }
    }

    public static class Validator {
        
        private static boolean hasSameCMD(CMD first, CMD second) {
            return first.compareTo(second) == 0;
        }

        private static boolean notNull(String string) {
            return string != null;
        }

        public static boolean isLoginRequest(ConnectionData request) {
            return hasSameCMD(CMD.LOGIN_REQUEST, request.cmd) && notNull(request.senderUsername) && notNull(request.senderPassword);
        }

        public static boolean isLogoutRequest(ConnectionData request) {
            return hasSameCMD(CMD.LOGOUT_REQUEST, request.cmd) && notNull(request.senderUsername);
        }

        public static boolean isAddFriendRequest(ConnectionData request) {
            return hasSameCMD(CMD.ADD_FRIEND_REQUEST, request.cmd) && notNull(request.senderUsername) && notNull(request.friendUsername);
        }

        public static boolean isFriendListRequest(ConnectionData request) {
            return hasSameCMD(CMD.FRIEND_LIST_REQUEST, request.cmd) && notNull(request.senderUsername);
        }

        public static boolean isScoreRequest(ConnectionData request) {
            return hasSameCMD(CMD.SCORE_REQUEST, request.cmd) && notNull(request.senderUsername);
        }

        public static boolean isLeaderboardRequest(ConnectionData request) {
            return hasSameCMD(CMD.LEADERBOARD_REQUEST, request.cmd) && notNull(request.senderUsername);
        }

        public static boolean isSuccessResponse(ConnectionData request) {
            return hasSameCMD(CMD.SUCCESS_RESPONSE, request.cmd);
        }

        public static boolean isFailResponse(ConnectionData request) {
            return hasSameCMD(CMD.FAIL_RESPONSE, request.cmd) && notNull(request.message);
        }
    }
}
