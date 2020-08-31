package com.domenico.server;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.function.Function;

//TODO this doc
public class TimeIsUp<T> extends TimerTask {

    private final Consumer<T[]> function;
    private final T[] args;

    public TimeIsUp(Consumer<T[]> function, final T[] args) {
        this.function = function;
        this.args = args;
    }

    @SafeVarargs
    public static <T> TimerTask schedule(Consumer<T[]> function, long delay, final T ...args) {
        Timer timer = new Timer();
        TimerTask timerTask = new TimeIsUp<>(function, args);
        timer.schedule(timerTask, delay);
        return timerTask;
    }

    @Override
    public void run() {
        function.accept(args);
    }
}
