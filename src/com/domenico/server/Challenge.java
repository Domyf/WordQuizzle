package com.domenico.server;

public class Challenge {

    private enum State { SENT, DECLINED, STARTED, FINISHED }
    private String from;
    private String to;
    private State state;

}
