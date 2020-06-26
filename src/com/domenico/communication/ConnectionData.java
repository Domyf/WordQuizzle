package com.domenico.communication;

import java.util.Arrays;

/**
 * This class represents the data passed by the client and the server in all the TCP and UDP communications. An object
 * of this class has a CMD which indicates what kind of request or response the object represents.
 * A ConnectionData object also an array that contains all the parameters and the toString method returns a string that
 * represents the object and which can be sent through the network.
 */
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
        /**
         * Compares the two CMDs passed as argument. Returns true if the two CMDs are the same, false otherwise.
         * @param first the first of the two CMDs that should be compared
         * @param second the second of the two CMDs that should be compared
         * @return true if the CMDs are the same, false otherwise
         */
        private static boolean hasSameCMD(CMD first, CMD second) {
            return first.compareTo(second) == 0;
        }

        /**
         * Checks if the specified String object is null. The return value is true if the string is not null,
         * false otherwise.
         * @param string the string that should be evaluated
         * @return true if the given string is not null, false otherwise
         */
        private static boolean notNull(String string) {
            return string != null;
        }

        /**
         * Checks if the given request represents a valid login request, which means it has the right CMD and has not
         * null parameters. The return value is true if the given request is a valid login request, false otherwise.
         * @param request the request that should be evaluated
         * @return true if the request has the login request's cmd and not null username and password, false otherwise.
         */
        public static boolean isLoginRequest(ConnectionData request) {
            return hasSameCMD(CMD.LOGIN_REQUEST, request.cmd) && notNull(request.senderUsername) && notNull(request.senderPassword);
        }

        /**
         * Checks if the given request represents a valid logout request, which means it has the right CMD and has not
         * null parameters. The return value is true if the given request is a valid logout request, false otherwise.
         * @param request the request that should be evaluated
         * @return true if the request has the logout request's cmd and not null username parameter, false otherwise.
         */
        public static boolean isLogoutRequest(ConnectionData request) {
            return hasSameCMD(CMD.LOGOUT_REQUEST, request.cmd) && notNull(request.senderUsername);
        }

        /**
         * Checks if the given request represents a valid add friend request, which means it has the right CMD and has not
         * null parameters. The return value is true if the given request is a valid add friend request, false otherwise.
         * @param request the request that should be evaluated
         * @return true if the request has the add friend request's cmd and not null username and friend's username,
         * false otherwise.
         */
        public static boolean isAddFriendRequest(ConnectionData request) {
            return hasSameCMD(CMD.ADD_FRIEND_REQUEST, request.cmd) && notNull(request.senderUsername) && notNull(request.friendUsername);
        }

        /**
         * Checks if the given request represents a valid friend list request, which means it has the right CMD and has not
         * null parameters. The return value is true if the given request is a valid friend list request, false otherwise.
         * @param request the request that should be evaluated
         * @return true if the request has the friend list request's cmd and not null username parameter, false otherwise.
         */
        public static boolean isFriendListRequest(ConnectionData request) {
            return hasSameCMD(CMD.FRIEND_LIST_REQUEST, request.cmd) && notNull(request.senderUsername);
        }

        /**
         * Checks if the given request represents a valid score request, which means it has the right CMD and has not
         * null parameters. The return value is true if the given request is a valid score request, false otherwise.
         * @param request the request that should be evaluated
         * @return true if the request has the score request's cmd and not null username parameter, false otherwise.
         */
        public static boolean isScoreRequest(ConnectionData request) {
            return hasSameCMD(CMD.SCORE_REQUEST, request.cmd) && notNull(request.senderUsername);
        }

        /**
         * Checks if the given request represents a valid leaderboard request, which means it has the right CMD and has not
         * null parameters. The return value is true if the given request is a valid leaderboard request, false otherwise.
         * @param request the request that should be evaluated
         * @return true if the request has the leaderboard request's cmd and not null username parameter, false otherwise.
         */
        public static boolean isLeaderboardRequest(ConnectionData request) {
            return hasSameCMD(CMD.LEADERBOARD_REQUEST, request.cmd) && notNull(request.senderUsername);
        }

        /**
         * Checks if the given response represents a valid success response, which means it has the right CMD.
         * The return value is true if the given response is a valid success response, false otherwise.
         * @param response the response that should be evaluated
         * @return true if the response has the success response's cmd, false otherwise.
         */
        public static boolean isSuccessResponse(ConnectionData response) {
            return hasSameCMD(CMD.SUCCESS_RESPONSE, response.cmd);
        }

        /**
         * Checks if the given response represents a valid fail response, which means it has the right CMD and not null
         * message.
         * The return value is true if the given response is a valid fail response, false otherwise.
         * @param response the response that should be evaluated
         * @return true if the response has the fail response's cmd and not null message, false otherwise.
         */
        public static boolean isFailResponse(ConnectionData response) {
            return hasSameCMD(CMD.FAIL_RESPONSE, response.cmd) && notNull(response.message);
        }
    }
}
