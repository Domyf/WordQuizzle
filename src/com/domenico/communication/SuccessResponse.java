package com.domenico.communication;

public class SuccessResponse extends Response {

    public static final String COMMAND = "SUCCESS";

    @Override
    public String toString() {
        return COMMAND;
    }
}
