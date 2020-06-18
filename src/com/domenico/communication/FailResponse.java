package com.domenico.communication;

public class FailResponse extends Response {
    public static final String COMMAND = "FAIL";

    private String failMessage;

    public FailResponse(String failMessage) {
        super();
        this.failMessage = failMessage;
    }

    @Override
    String toLine() {
        return COMMAND+" "+failMessage;
    }
}
