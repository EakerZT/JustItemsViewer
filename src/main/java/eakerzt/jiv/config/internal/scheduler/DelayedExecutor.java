package eakerzt.jiv.config.internal.scheduler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executes delayed commands and flushes pending delayed commands during shutdown.
 */
public final class DelayedExecutor implements DelayedTaskScheduler, AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(DelayedExecutor.class.getName());
    private static final String DEFAULT_THREAD_NAME_PREFIX = "DeduplicatingRunner Delayed Executor";

    private final ScheduledExecutorService service;
    private final Duration shutdownTimeout;
    private final Set<ScheduledTask> scheduledTasks = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    /**
     * Creates an executor with a single daemon worker thread.
     *
     * @param shutdownTimeout how long shutdown waits for flushed commands to finish before forcing shutdown
     */
    public DelayedExecutor(Duration shutdownTimeout) {
        this(shutdownTimeout, DEFAULT_THREAD_NAME_PREFIX);
    }

    /**
     * Creates an executor with a single daemon worker thread using names based on {@code threadNamePrefix}.
     *
     * @param shutdownTimeout how long shutdown waits for flushed commands to finish before forcing shutdown
     * @param threadNamePrefix the prefix for worker thread names
     */
    public DelayedExecutor(Duration shutdownTimeout, String threadNamePrefix) {
        this(shutdownTimeout, createDefaultService(threadNamePrefix));
    }

    /**
     * Creates an executor backed by {@code service}.
     *
     * @param shutdownTimeout how long shutdown waits for flushed commands to finish before forcing shutdown
     * @param service the scheduler used for delayed execution
     */
    public DelayedExecutor(Duration shutdownTimeout, ScheduledExecutorService service) {
        this.shutdownTimeout = Objects.requireNonNull(shutdownTimeout, "shutdownTimeout");
        this.service = Objects.requireNonNull(service, "service");
    }

    static ScheduledThreadPoolExecutor createDefaultService(String threadNamePrefix) {
        Objects.requireNonNull(threadNamePrefix, "threadNamePrefix");

        ScheduledThreadPoolExecutor service = new ScheduledThreadPoolExecutor(
            1,
            createThreadFactory(threadNamePrefix)
        );
        service.setRemoveOnCancelPolicy(true);
        service.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        return service;
    }

    private static ThreadFactory createThreadFactory(String threadNamePrefix) {
        final AtomicInteger threadNumber = new AtomicInteger(1);
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable command) {
                Thread thread = new Thread(command, threadNamePrefix + " " + threadNumber.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        };
    }

    @Override
    public Future<?> schedule(Runnable command, Duration delay) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(delay, "delay");

        if (isShutdown()) {
            return runImmediately(command);
        }

        ScheduledTask scheduledTask = new ScheduledTask(command);
        scheduledTasks.add(scheduledTask);
        try {
            Future<?> future = service.schedule(scheduledTask, toDelayNanos(delay), TimeUnit.NANOSECONDS);
            scheduledTask.setFuture(future);
            TrackedFuture trackedFuture = new TrackedFuture(scheduledTask);
            if (isShutdown()) {
                flushScheduledTask(scheduledTask);
            }
            return trackedFuture;
        } catch (RejectedExecutionException e) {
            scheduledTasks.remove(scheduledTask);
            if (isShutdown()) {
                return runImmediately(command);
            }
            throw e;
        } catch (RuntimeException e) {
            scheduledTasks.remove(scheduledTask);
            throw e;
        }
    }

    /**
     * Flushes pending delayed commands and shuts down the backing scheduler.
     */
    public void shutdown() {
        if (!shutdown.compareAndSet(false, true)) {
            return;
        }

        runScheduledTasksImmediately();
        service.shutdown();
        try {
            if (!service.awaitTermination(shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                forceShutdown("Timed out waiting for delayed tasks to finish.");
            }
        } catch (InterruptedException ignored) {
            forceShutdown("Interrupted while waiting for delayed tasks to finish.");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Equivalent to {@link #shutdown()}.
     */
    @Override
    public void close() {
        shutdown();
    }

    private boolean isShutdown() {
        return shutdown.get() || service.isShutdown();
    }

    private void runScheduledTasksImmediately() {
        for (ScheduledTask scheduledTask : new ArrayList<ScheduledTask>(scheduledTasks)) {
            flushScheduledTask(scheduledTask);
        }
    }

    private void flushScheduledTask(ScheduledTask scheduledTask) {
        if (!scheduledTasks.remove(scheduledTask)) {
            return;
        }
        if (scheduledTask.claimForShutdown()) {
            runScheduledTaskDuringShutdown(scheduledTask);
        }
    }

    private void runScheduledTaskDuringShutdown(ScheduledTask scheduledTask) {
        try {
            scheduledTask.command().run();
            scheduledTask.completion().complete(null);
        } catch (Throwable e) {
            scheduledTask.completion().completeExceptionally(e);
            LOGGER.log(Level.SEVERE, "Failed to execute delayed task during shutdown.", e);
        }
    }

    private void forceShutdown(String message) {
        List<Runnable> droppedTasks = service.shutdownNow();
        if (droppedTasks.isEmpty()) {
            LOGGER.log(Level.SEVERE, message + " Forcing shutdown.");
        } else {
            LOGGER.log(Level.SEVERE, message + " Forcing shutdown. " + droppedTasks.size() + " delayed tasks never started.");
        }
    }

    private static Future<?> runImmediately(Runnable command) {
        CompletableFuture<Void> future = new CompletableFuture<Void>();
        try {
            command.run();
            future.complete(null);
        } catch (RuntimeException | LinkageError e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    private static long toDelayNanos(Duration delay) {
        if (delay.isNegative() || delay.isZero()) {
            return 0L;
        }
        try {
            return delay.toNanos();
        } catch (ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }

    private final class TrackedFuture implements Future<Void> {
        private final CompletableFuture<Void> delegate;
        private final ScheduledTask scheduledTask;

        private TrackedFuture(ScheduledTask scheduledTask) {
            this.delegate = scheduledTask.completion();
            this.scheduledTask = scheduledTask;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return scheduledTask.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return delegate.isCancelled();
        }

        @Override
        public boolean isDone() {
            return delegate.isDone();
        }

        @Override
        public Void get() throws InterruptedException, ExecutionException {
            return delegate.get();
        }

        @Override
        public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return delegate.get(timeout, unit);
        }
    }

    private final class ScheduledTask implements Runnable {
        private final Runnable command;
        private final CompletableFuture<Void> completion = new CompletableFuture<Void>();
        private final AtomicBoolean claimed = new AtomicBoolean(false);
        private volatile Future<?> future;

        private ScheduledTask(Runnable command) {
            this.command = command;
        }

        @Override
        public void run() {
            if (!claimed.compareAndSet(false, true)) {
                return;
            }
            try {
                command.run();
                completion.complete(null);
            } catch (RuntimeException | Error e) {
                completion.completeExceptionally(e);
                throw e;
            } finally {
                scheduledTasks.remove(this);
            }
        }

        public Runnable command() {
            return command;
        }

        public CompletableFuture<Void> completion() {
            return completion;
        }

        public void setFuture(Future<?> future) {
            this.future = future;
            if (claimed.get() && !future.isDone()) {
                future.cancel(false);
            }
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            if (!claimed.compareAndSet(false, true)) {
                return false;
            }

            scheduledTasks.remove(this);
            Future<?> currentFuture = Objects.requireNonNull(this.future, "future");
            currentFuture.cancel(mayInterruptIfRunning);
            completion.cancel(false);
            return true;
        }

        public boolean claimForShutdown() {
            if (!claimed.compareAndSet(false, true)) {
                return false;
            }

            Future<?> currentFuture = this.future;
            if (currentFuture != null) {
                if (currentFuture.isCancelled()) {
                    completion.cancel(false);
                    return false;
                }
                currentFuture.cancel(false);
            }
            return true;
        }
    }
}
