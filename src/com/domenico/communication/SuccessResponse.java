package com.domenico.communication;

public class SuccessResponse extends Response {

    public static final String COMMAND = "SUCCESS";

    @Override
    String toLine() {
        return COMMAND;
    }
}
