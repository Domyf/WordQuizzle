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
        CHALLENGE_START,
        CHALLENGE_END,
        CHALLENGE_WORD,
        SCORE_REQUEST,
        LEADERBOARD_REQUEST,
        SUCCESS_RESPONSE,
        FAIL_RESPONSE
    }

    private static final String PARAMETERS_DIVIDER = " ";
    private static final String RESPONSE_DATA_DIVIDER = ";";
    private final CMD cmd;                //The command of the request/response
    private final String[] params;        //The parameters of the request/response
    private String senderUsername;  //The username of who sent the message. It is always used
    private String senderPassword;  //The password of who sent the message.
    private String friendUsername;  //The friend's username. Used for the ADD_FRIEND request
    private String responseData;    //The data which is attached to a message (used as failure message by the fail response or by the success response)

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

    public String getUsername() { return senderUsername; }

    public String getPassword() { return senderPassword; }

    public String getFriendUsername() { return friendUsername; }

    public String getResponseData() { return responseData; }

    /** Splits the response data into an array. Used when the message is that kind of custom messages */
    public String[] splitResponseData() { return responseData.split(RESPONSE_DATA_DIVIDER); }

    /**
     * Transforms this into a string. The returned string has the following pattern: <command> <parameter1> <parameter2> ...
     * @return returns a string that represents this object.
     */
    @Override
    public String toString() {
        String parameters = Utils.stringify(params, PARAMETERS_DIVIDER);
        if (parameters.isEmpty())
            return cmd.toString();
        return cmd.toString()+PARAMETERS_DIVIDER+parameters;
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
            String[] splittedLine = line.split(PARAMETERS_DIVIDER);
            String[] params = new String[0];
            String cmd = splittedLine[0];
            if (splittedLine.length > 1)
                params = Arrays.copyOfRange(splittedLine, 1, splittedLine.length);

            try {
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
                    case CHALLENGE_START:
                        if (params.length == 1)
                            return newChallengeStart(params[0]);
                        break;
                    case CHALLENGE_END:
                        if (params.length == 1)
                            return newChallengeEnd(params[0]);
                        break;
                    case CHALLENGE_WORD:
                        if (params.length >= 1) {
                            String word = params.length == 1 ? params[0] : Utils.stringify(params, " ");
                            return newChallengeWord(word);
                        }
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
                        String paramsRow = Utils.stringify(params, " ");
                        return newFailResponse(paramsRow);
                }
            } catch (IllegalArgumentException ignored) {}   //ignored because it will return Invalid Command
            return newFailResponse("Invalid command");
        }

        /**
         * Builds a ConnectionData object that represents a login request
         * @param username the username of who is sending the request
         * @param password the password of who is sending the request
         * @param udpPort the udpPort on which the user can receive data via UDP protocol
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
         *
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
         * Builds a ConnectionData object that represents a challenge start response
         *
         * @return a ConnectionData object that represents a challenge start response
         */
        public static ConnectionData newChallengeStart(long maxChallengeLength, int challengeWords, String nextItWord) {
            String data = Utils.stringify(RESPONSE_DATA_DIVIDER, maxChallengeLength, challengeWords, nextItWord);
            return newCustomFromData(CMD.CHALLENGE_START, data);
        }

        /**
         * Builds a ConnectionData object that represents a challenge start response
         *
         * @return a ConnectionData object that represents a challenge start response
         */
        public static ConnectionData newChallengeStart(String data) {
            return newCustomFromData(CMD.CHALLENGE_START, data);
        }

        /**
         * Builds a ConnectionData object that represents a challenge end response
         *
         * @return a ConnectionData object that represents a challenge end response
         */
        public static ConnectionData newChallengeEnd(int correct, int wrong, int notransl, int yourscore, int otherscore, int extrapoints) {
            String data = Utils.stringify(RESPONSE_DATA_DIVIDER, correct, wrong, notransl, yourscore, otherscore, extrapoints);
            return newCustomFromData(CMD.CHALLENGE_END, data);
        }

        /**
         * Builds a ConnectionData object that represents a challenge end response
         *
         * @return a ConnectionData object that represents a challenge end response
         */
        public static ConnectionData newChallengeEnd(String data) {
            return newCustomFromData(CMD.CHALLENGE_END, data);
        }

        /**
         * Build a ConnectionData object that represents the next word that the user should translate or the translated
         * word. The first case occurs when the server sends this kind of object to the user while the second case occurs
         * when the user sends this type of ConnectionData object to the server.
         *
         * @param word the word that the user should translate
         * @return a ConnectionData object that represents the next word that the user should translate
         */
        public static ConnectionData newChallengeWord(String word) {
            ConnectionData connectionData = new ConnectionData(CMD.CHALLENGE_WORD, new String[]{word});
            connectionData.responseData = word;
            return connectionData;
        }

        /**
         * Builds a ConnectionData object that represents a score request
         *
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

        /**
         * Builds a ConnectionData object that represents a success response with data attached.
         *
         * @param data data to attach
         * @return a ConnectionData object that represents a success response
         */
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

        /**
         * Builds a ConnectionData object that represents a fail response
         *
         * @return a ConnectionData object that represents a fail response
         */
        public static ConnectionData newFailResponse() {
            return new ConnectionData(CMD.FAIL_RESPONSE, new String[]{});
        }

        /**
         * Builds a custom ConnectionData object with the given CMD field and given response data.
         *
         * @return a ConnectionData object that represents a custom message with a custom response data
         */
        private static ConnectionData newCustomFromData(CMD cmd, String data) {
            ConnectionData connectionData = new ConnectionData(cmd, new String[]{data});
            connectionData.responseData = data;
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
         * Checks if the given response represents a valid challenge end response, which means it has the right CMD.
         * The return value is true if the given response is a valid challenge response, false otherwise.
         *
         * @param response the response that should be evaluated
         * @return true if the response has the challenge response's cmd, false otherwise.
         */
        public static boolean isChallengeEnd(ConnectionData response) {
            return hasSameCMD(CMD.CHALLENGE_END, response.cmd);
        }

        /**
         * Checks if the given data represents a valid challenge word data, which means it has the right CMD and
         * a word as response data.
         * The return value is true if the given data is a valid challenge word data, false otherwise.
         *
         * @param data the data that should be evaluated
         * @return true if the data has the challenge data's cmd and a word as response data, false otherwise.
         */
        public static boolean isChallengeWord(ConnectionData data) {
            return hasSameCMD(CMD.CHALLENGE_WORD, data.cmd) && notNull(data.responseData);
        }

        /**
         * Checks if the given data represents a valid challenge start data, which means it has the right CMD and
         * a custom response data that contains how much long can this challenge run, how many words this challenge
         * will have and the first word.
         * The return value is true if the given data is a valid challenge start data, false otherwise.
         *
         * @param data the data that should be evaluated
         * @return true if the data has the challenge data's cmd and contains how much long can this challenge run,
         * how many words this challenge will have and the first word, false otherwise.
         */
        public static boolean isChallengeStart(ConnectionData data) {
            return hasSameCMD(CMD.CHALLENGE_START, data.cmd) && notNull(data.responseData)
                    && data.responseData.indexOf(RESPONSE_DATA_DIVIDER) != data.responseData.lastIndexOf(RESPONSE_DATA_DIVIDER);
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
         * Checks if the given response represents a valid fail response, which means it has the right CMD.
         * The return value is true if the given response is a valid fail response, false otherwise.
         *
         * @param response the response that should be evaluated
         * @return true if the response has the fail response's cmd, false otherwise.
         */
        public static boolean isFailResponse(ConnectionData response) {
            return hasSameCMD(CMD.FAIL_RESPONSE, response.cmd);
        }
    }
}
