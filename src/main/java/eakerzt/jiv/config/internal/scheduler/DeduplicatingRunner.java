package eakerzt.jiv.config.internal.scheduler;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Future;

/**
 * Runs only the latest command after no replacements have arrived for the configured delay.
 */
public final class DeduplicatingRunner {
    private final DelayedTaskScheduler scheduler;
    private final Duration delay;
    private Future<?> future;

    /**
     * Creates a runner that schedules the latest command through {@code scheduler}.
     *
     * @param delay the quiet period required before a pending command runs
     * @param scheduler the scheduler used for delayed execution
     */
    public DeduplicatingRunner(Duration delay, DelayedTaskScheduler scheduler) {
        this.delay = Objects.requireNonNull(delay, "delay");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    }

    /**
     * Cancels the previous pending command and schedules the replacement.
     *
     * @param runnable the latest command to run
     * @return the future for the latest scheduled command
     */
    public synchronized Future<?> run(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");

        Future<?> pendingFuture = future;
        if (pendingFuture != null) {
            pendingFuture.cancel(false);
        }

        ScheduledRun scheduledRun = new ScheduledRun(runnable);
        Future<?> scheduledFuture = scheduler.schedule(scheduledRun, delay);
        scheduledRun.setFuture(scheduledFuture);
        future = scheduledFuture;
        if (scheduledFuture.isDone()) {
            clearFutureIfActive(scheduledFuture);
        }
        return scheduledFuture;
    }

    private synchronized void clearFutureIfActive(Future<?> completedFuture) {
        if (future == completedFuture) {
            future = null;
        }
    }

    private final class ScheduledRun implements Runnable {
        private final Runnable runnable;
        private Future<?> scheduledFuture;

        private ScheduledRun(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                runnable.run();
            } finally {
                Future<?> completedFuture = scheduledFuture;
                if (completedFuture != null) {
                    clearFutureIfActive(completedFuture);
                }
            }
        }

        private void setFuture(Future<?> scheduledFuture) {
            this.scheduledFuture = scheduledFuture;
        }
    }
}
