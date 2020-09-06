package com.domenico.server;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

/** Class that can schedule functions call after a specific amount of time. Used to call a specific function when
 * the challenge time is up. */
public class TimeIsUp<T> extends TimerTask {

    private static final Timer timer = new Timer(); //the timer that will run each function
    private final Consumer<T[]> function;   //scheduled function
    private final T[] args; //function's arguments

    private TimeIsUp(Consumer<T[]> function, final T[] args) {
        this.function = function;
        this.args = args;
    }

    /**
     * Schedules the given function after the specified delay. The given arguments are passed to the function when
     * called in the future.
     * @param function the scheduled function
     * @param delay after how many milliseconds the function should be called
     * @param args the arguments to pass to the function
     * @param <T> the argument's type
     * @return a {@link TimerTask} object that represents the scheduled function
     */
    @SafeVarargs
    public static <T> TimerTask schedule(Consumer<T[]> function, long delay, final T ...args) {
        TimerTask timerTask = new TimeIsUp<>(function, args);
        timer.schedule(timerTask, delay);
        return timerTask;
    }

    @Override
    public void run() {
        function.accept(args);  //calls the function
    }
}
