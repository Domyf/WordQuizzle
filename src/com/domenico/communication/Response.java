package com.domenico.communication;

public abstract class Response {

    public static Response parseResponse(String line) {
        String[] splittedLine = line.split(" ");
        switch (splittedLine[0]) {
            case SuccessResponse.COMMAND:
                return new SuccessResponse();
            case FailResponse.COMMAND:
                return new FailResponse(splittedLine);
        }

        return null;
    }

    abstract String toLine();

    @Override
    public String toString() {
        return toLine();
    }
}
