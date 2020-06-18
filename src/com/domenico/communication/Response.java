package com.domenico.communication;

import java.util.Arrays;

public abstract class Response {

    public static Response parseResponse(String line) {
        String[] splittedLine = line.split(" ");
        String[] params = new String[0];
        if (splittedLine.length > 1)
            params = Arrays.copyOfRange(splittedLine, 1, splittedLine.length);
        switch (splittedLine[0]) {
            case SuccessResponse.COMMAND:
                return new SuccessResponse();
            case FailResponse.COMMAND:
                return new FailResponse(params);
        }

        return null;
    }
}
