package com.domenico.communication;

public class FailResponse extends Response {
    public static final String COMMAND = "FAIL";

    private String failMessage;

    public FailResponse(String[] params) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < params.length; i++) {
            builder.append(params[i]).append(" ");
        }
        this.failMessage = builder.toString();
    }

    public FailResponse(String failmessage) {
        this.failMessage = failmessage;
    }


    @Override
    public String toString() {
        return COMMAND+" "+failMessage;
    }
}
