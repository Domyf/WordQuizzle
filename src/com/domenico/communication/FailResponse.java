package com.domenico.communication;

public class FailResponse extends Response {
    public static final String COMMAND = "FAIL";

    private String failMessage;

    public FailResponse(String[] line) {
        super();
        this.failMessage = line[1];
    }

    @Override
    String toLine() {
        return COMMAND+" "+failMessage;
    }
}
