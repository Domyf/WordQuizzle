package com.domenico.server;

import java.util.Timer;
import java.util.TimerTask;

//TODO this doc
public class TimeIsUp extends TimerTask {

    private final TimeIsUpListener listener;
    private final Object[] params;

    private TimeIsUp(TimeIsUpListener listener, Object[] params) {
        this.listener = listener;
        this.params = params;
    }

    public static void schedule(TimeIsUpListener listener, long delay, Object... params) {
        Timer timer = new Timer();
        timer.schedule(new TimeIsUp(listener, params), delay);
    }

    @Override
    public void run() {
        listener.timeIsUp(params);
    }
}
