package com.domenico.communication;

public abstract class Request {

    public static Request parseRequest(String line) {
        String[] splittedLine = line.split(" ");
        switch (splittedLine[0]) {
            case LoginRequest.COMMAND:
                return new LoginRequest(splittedLine);
            case ScoreRequest.COMMAND:
                return new ScoreRequest(splittedLine);
        }

        return null;
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
