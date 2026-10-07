package eakerzt.jiv.config.internal.scheduler;

import java.time.Duration;
import java.util.concurrent.Future;

/**
 * Schedules commands to run after a delay.
 */
public interface DelayedTaskScheduler {
    /**
     * Schedules {@code command} to run after {@code delay}.
     *
     * @param command the work to run
     * @param delay the requested delay before execution
     * @return a future representing the scheduled or immediate execution
     */
    Future<?> schedule(Runnable command, Duration delay);
}
