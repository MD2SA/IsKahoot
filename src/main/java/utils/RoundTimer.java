package utils;

public class RoundTimer implements AutoCloseable {

    private final Object monitor = new Object();

    private Thread worker;
    private Runnable task;
    private long deadlineNanos;
    private boolean shutdown;

    public void schedule(int seconds, Runnable callback) {
        if (seconds < 0)
            throw new IllegalArgumentException("seconds must not be negative");
        if (callback == null)
            throw new IllegalArgumentException("callback must not be null");

        synchronized (monitor) {
            if (shutdown)
                throw new IllegalStateException("timer is closed");

            task = callback;
            deadlineNanos = System.nanoTime() + seconds * 1_000_000_000L;

            if (worker == null) {
                worker = new Thread(this::runLoop, "round-timer");
                worker.setDaemon(true);
                worker.start();
            }

            monitor.notifyAll();
        }
    }

    public void cancel() {
        synchronized (monitor) {
            task = null;
            monitor.notifyAll();
        }
    }

    @Override
    public void close() {
        synchronized (monitor) {
            shutdown = true;
            task = null;
            monitor.notifyAll();
        }
    }

    @SuppressWarnings({"PMD.CognitiveComplexity", "PMD.CyclomaticComplexity", "PMD.AvoidCatchingGenericException"})
    private void runLoop() {
        while (true) {
            Runnable expired;

            synchronized (monitor) {
                while (!shutdown && task == null) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException ignored) {
                        if (shutdown)
                            return;
                    }
                }

                if (shutdown)
                    return;

                long remainingNanos = deadlineNanos - System.nanoTime();

                while (remainingNanos > 0 && !shutdown && task != null) {
                    try {
                        monitor.wait(remainingNanos / 1_000_000L, (int) (remainingNanos % 1_000_000L));
                    } catch (InterruptedException ignored) {
                        if (shutdown)
                            return;
                    }

                    remainingNanos = deadlineNanos - System.nanoTime();
                }

                if (shutdown)
                    return;

                if (task == null)
                    continue;

                expired = task;
                task = null;
            }

            try {
                expired.run();
            } catch (RuntimeException e) {
                Thread current = Thread.currentThread();
                current.getUncaughtExceptionHandler().uncaughtException(current, e);
            }
        }
    }
}
