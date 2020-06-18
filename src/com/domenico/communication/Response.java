package com.domenico.communication;

public abstract class Response {

    public static Response parseResponse(String line) {
        String[] splittedLine = line.split(" ");
        switch (splittedLine[0]) {
            case SuccessResponse.COMMAND:
                return new SuccessResponse();
            case FailResponse.COMMAND:
                String message = line.substring(splittedLine[0].length()+1);
                return new FailResponse(message);
        }

        return null;
    }

    abstract String toLine();

    @Override
    public String toString() {
        return toLine();
    }
}
