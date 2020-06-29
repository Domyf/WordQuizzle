package com.domenico.communication;

import com.domenico.shared.Utils;

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
        CHALLENGE_REQUEST,
        SCORE_REQUEST,
        LEADERBOARD_REQUEST,
        SUCCESS_RESPONSE,
        FAIL_RESPONSE
    }

    private CMD cmd;                //The command of the request/response
    private String[] params;        //The parameters of the request/response
    private String senderUsername;  //The username of who sent the message. It is always used
    private String senderPassword;  //The password of who sent the message.
    private String friendUsername;  //The friend's username. Used for the ADD_FRIEND request
    private String responseData;    //The data which is used as failure message by the fail response or as data by the success response

    /**
     * Private constructor. It creates this by using the given parameters. The attributes are set as null. The only way
     * to instantiate a ConnectionData is by using the methods available in the Factory class.
     * @param cmd the command
     * @param params the array of parameters
     */
    private ConnectionData(CMD cmd, String[] params) {
        this.cmd = cmd;
        this.params = params;
        senderUsername = null;
        senderPassword = null;
        friendUsername = null;
        responseData = null;
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

    public String getResponseData() {
        return responseData;
    }

    /**
     * Transforms this into a string. The returned string has the following pattern: <command> <parameter1> <parameter2> ...
     * @return returns a string that represents this object.
     */
    @Override
    public String toString() {
        String parameters = Utils.stringify(params, " ");
        return cmd.toString()+" "+parameters;
    }

    /**
     * Factory class for the ConnectionData object. This class has the methods to create a valid ConnectionData object.
     * In general the methods available can instantiate a specific request or response by parsing a string or by some
     * given arguments.
     */
    public static class Factory {
        /**
         * This method instantiate a ConnectionData object by parsing a given string. The given string should represent
         * a ConnectionData object like the string returned by ConnectionData.toString() method does.
         * @param line the string that should be parsed. It should start with the command and it should have the parameters
         *             if any. The command and the parameters should be separated by a blank character.
         * @return a valid ConnectionData object that represents the given line or null if the line doesn't represent a
         * valid ConnectionData object.
         */
        public static ConnectionData parseLine(String line) {
            String[] splittedLine = line.split(" ");
            String[] params = new String[0];
            String cmd = splittedLine[0];
            if (splittedLine.length > 1)
                params = Arrays.copyOfRange(splittedLine, 1, splittedLine.length);
            switch (CMD.valueOf(cmd)) {
                case LOGIN_REQUEST:
                    if (params.length == 3)
                        return newLoginRequest(params[0], params[1], Integer.parseUnsignedInt(params[2]));
                    break;
                case LOGOUT_REQUEST:
                    if (params.length == 1)
                        return newLogoutRequest(params[0]);
                    break;
                case ADD_FRIEND_REQUEST:
                    if (params.length == 2)
                        return newAddFriendRequest(params[0], params[1]);
                    break;
                case FRIEND_LIST_REQUEST:
                    if (params.length == 1)
                        return newFriendListRequest(params[0]);
                    break;
                case CHALLENGE_REQUEST:
                    if (params.length == 2)
                        return newChallengeRequest(params[0], params[1]);
                    break;
                case SCORE_REQUEST:
                    if (params.length == 1)
                        return newScoreRequest(params[0]);
                    break;
                case LEADERBOARD_REQUEST:
                    if (params.length == 1)
                        return newLeaderboardRequest(params[0]);
                case SUCCESS_RESPONSE:
                    if (params.length == 0) {
                        return newSuccessResponse();
                    } else {
                        String paramsRow = Utils.stringify(params, " ");
                        return newSuccessResponse(paramsRow);
                    }
                case FAIL_RESPONSE:
                    if (params.length > 0) {
                        String paramsRow = Utils.stringify(params, " ");
                        return newFailResponse(paramsRow);
                    }
            }

            return newFailResponse("Invalid command");
        }

        /**
         * Builds a ConnectionData object that represents a login request
         * @param username the username of who is sending the request
         * @param password the password of who is sending the request
         * @param udpPort
         * @return a ConnectionData object that represents a login request
         */
        public static ConnectionData newLoginRequest(String username, String password, int udpPort) {
            ConnectionData connectionData = new ConnectionData(CMD.LOGIN_REQUEST, new String[]{username, password, ""+udpPort});
            connectionData.senderUsername = username;
            connectionData.senderPassword = password;
            connectionData.responseData = ""+udpPort;
            return connectionData;
        }

        /**
         * Builds a ConnectionData object that represents a logout request
         * @param username the username of who is sending the request
         * @return a ConnectionData object that represents a logout request
         */
        public static ConnectionData newLogoutRequest(String username) {
            ConnectionData connectionData = new ConnectionData(CMD.LOGOUT_REQUEST, new String[]{username});
            connectionData.senderUsername = username;
            return connectionData;
        }

        /**
         * Builds a ConnectionData object that represents an add friend request
         * @param username the username of who is sending the request
         * @param friendUsername the friend's username
         * @return a ConnectionData object that represents an add friend request
         */
        public static ConnectionData newAddFriendRequest(String username, String friendUsername) {
            ConnectionData connectionData = new ConnectionData(CMD.ADD_FRIEND_REQUEST, new String[]{username, friendUsername});
            connectionData.senderUsername = username;
            connectionData.friendUsername = friendUsername;
            return connectionData;
        }

        /**
         * Builds a ConnectionData object that represents a friend list request
         * @param username the username of who is sending the request
         * @return a ConnectionData object that represents a friend list request
         */
        public static ConnectionData newFriendListRequest(String username) {
            ConnectionData connectionData = new ConnectionData(CMD.FRIEND_LIST_REQUEST, new String[]{username});
            connectionData.senderUsername = username;
            return connectionData;
        }

        /**
         * Builds a ConnectionData object that represents an challenge request
         * @param username the username of who is sending the request
         * @param friendUsername the friend's username
         * @return a ConnectionData object that represents a challenge request
         */
        public static ConnectionData newChallengeRequest(String username, String friendUsername) {
            ConnectionData connectionData = new ConnectionData(CMD.CHALLENGE_REQUEST, new String[]{username, friendUsername});
            connectionData.senderUsername = username;
            connectionData.friendUsername = friendUsername;
            return connectionData;
        }

        /**
         * Builds a ConnectionData object that represents a score request
         * @param username the username of who is sending the request
         * @return a ConnectionData object that represents a score request
         */
        public static ConnectionData newScoreRequest(String username) {
            ConnectionData connectionData = new ConnectionData(CMD.SCORE_REQUEST, new String[]{username});
            connectionData.senderUsername = username;
            return connectionData;
        }

        /**
         * Builds a ConnectionData object that represents a leaderboard request
         * @param username the username of who is sending the request
         * @return a ConnectionData object that represents a leaderboard request
         */
        public static ConnectionData newLeaderboardRequest(String username) {
            ConnectionData connectionData = new ConnectionData(CMD.LEADERBOARD_REQUEST, new String[]{username});
            connectionData.senderUsername = username;
            return connectionData;
        }

        /**
         * Builds a ConnectionData object that represents a success response
         * @return a ConnectionData object that represents a success response
         */
        public static ConnectionData newSuccessResponse() {
            return new ConnectionData(CMD.SUCCESS_RESPONSE, new String[]{});
        }

        // TODO: 26/06/2020 this doc
        public static ConnectionData newSuccessResponse(String data) {
            ConnectionData connectionData = new ConnectionData(CMD.SUCCESS_RESPONSE, new String[]{data});
            connectionData.responseData = data;
            return connectionData;
        }

        /**
         * Builds a ConnectionData object that represents a fail response
         * @param failmessage the failure message that the fail response should have
         * @return a ConnectionData object that represents a fail response
         */
        public static ConnectionData newFailResponse(String failmessage) {
            ConnectionData connectionData = new ConnectionData(CMD.FAIL_RESPONSE, new String[]{failmessage});
            connectionData.responseData = failmessage;
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
         * Checks if the given request represents a valid challenge request, which means it has the right CMD and has not
         * null parameters. The return value is true if the given request is a valid challenge request, false otherwise.
         * @param request the request that should be evaluated
         * @return true if the request has the challenge request's cmd and not null username and friend's username,
         * false otherwise.
         */
        public static boolean isChallengeRequest(ConnectionData request) {
            return hasSameCMD(CMD.CHALLENGE_REQUEST, request.cmd) && notNull(request.senderUsername) && notNull(request.friendUsername);
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
            return hasSameCMD(CMD.FAIL_RESPONSE, response.cmd) && notNull(response.responseData);
        }
    }
}
