package utils.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ThreadPool {

    private final BlockingQueue<Runnable> taskQueue;
    private final List<Thread> workers;
    private final AtomicBoolean shutdownInitiated = new AtomicBoolean();
    private boolean workersStarted;

    public ThreadPool(int numThreads) {
        if (numThreads <= 0)
            throw new IllegalArgumentException("Number of threads must be positive");
        this.taskQueue = new BlockingQueue<>();
        this.workers = new ArrayList<>(numThreads);

        for (int i = 0; i < numThreads; i++) {
            Thread worker = new Thread(this::workerLoop, "iskahoot-worker-" + i);
            workers.add(worker);
        }
    }

    public synchronized void submit(Runnable runnable) {
        if (shutdownInitiated.get())
            throw new IllegalStateException("Thread pool is shutdown");
        taskQueue.put(Objects.requireNonNull(runnable, "runnable"));
        if (!workersStarted) {
            workersStarted = true;
            workers.forEach(Thread::start);
        }
    }

    public synchronized void shutdown() {
        shutdownInitiated.set(true);
        for (Thread worker : workers)
            worker.interrupt();
    }

    public synchronized List<Runnable> shutdownNow() {
        shutdownInitiated.set(true);
        List<Runnable> remainingTasks = new ArrayList<>();
        taskQueue.drainTo(remainingTasks);
        for (Thread worker : workers)
            worker.interrupt();
        return remainingTasks;
    }

    public boolean isShutdown() {
        return shutdownInitiated.get();
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private void workerLoop() {
        while (!shutdownInitiated.get() || !taskQueue.isEmpty()) {
            try {
                Runnable task = taskQueue.take();
                try {
                    task.run();
                } catch (RuntimeException e) {
                    Thread current = Thread.currentThread();
                    current.getUncaughtExceptionHandler().uncaughtException(current, e);
                }
            } catch (InterruptedException e) {
                if (shutdownInitiated.get()) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
