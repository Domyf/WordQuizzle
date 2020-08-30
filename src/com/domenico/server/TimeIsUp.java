package com.domenico.server;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.function.Function;

//TODO this doc
public class TimeIsUp<T> extends TimerTask {

    private final Consumer<T[]> function;
    private final T[] args;

    public TimeIsUp(Consumer<T[]> function, T ...args) {
        this.function = function;
        this.args = args;
    }

    public static <T> void schedule(Consumer<T[]> function, long delay, T ...args) {
        Timer timer = new Timer();
        timer.schedule(new TimeIsUp<T>(function, args), delay);
    }

    @Override
    public void run() {
        function.accept(args);
    }
}
