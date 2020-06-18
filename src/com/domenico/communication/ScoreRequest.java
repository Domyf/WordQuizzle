package com.domenico.communication;

public class ScoreRequest extends Request {

    public static final String COMMAND = "SCORE_REQUEST";

    private String username;

    public ScoreRequest(String[] line) {
        this.username = line[1];
    }

    @Override
    String toLine() {
        return COMMAND+" "+username;
    }
}
