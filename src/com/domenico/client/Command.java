package com.domenico.client;

import java.util.Arrays;

/** This class represent a command with its cmd and its parameters */
public class Command {

    private String cmd;
    private String[] params;

    /** Constructor that build the command from the line typed by the user */
    public Command(String line) {
        String[] splittedLine = line.split(" ");
        if (splittedLine.length > 0)
            cmd = splittedLine[0];
        if (splittedLine.length > 1)
            params = Arrays.copyOfRange(splittedLine, 1, splittedLine.length);
    }

    /** Getter method for the cmd string */
    public String getCmd() {
        return cmd;
    }

    /** Returns the param defined by the given index. It can throw IndexOutOfBoundException if the index is not valid */
    public String getParam(int index) {
        return params[index];
    }

    /** Returns true if this command has as much params as the integer passed by argument, false otherwise */
    public boolean hasParams(int howMany) {
        return params != null && params.length == howMany;
    }
}
