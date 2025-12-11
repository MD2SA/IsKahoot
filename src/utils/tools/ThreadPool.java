package utils.tools;

import java.util.ArrayList;
import java.util.List;

public class ThreadPool {

        private final BlockingQueue<Runnable> taskQueue;
        private final List<WorkerThread> workers;
        private volatile boolean shutdownInitiated = false;

        public ThreadPool(int numThreads) {
                if (numThreads <= 0)
                        throw new IllegalArgumentException("Number of threads must be positive");
                this.taskQueue = new BlockingQueue<>();
                this.workers = new ArrayList<>(numThreads);

                for (int i = 0; i < numThreads; i++) {
                        WorkerThread worker = new WorkerThread();
                        workers.add(worker);
                        worker.start();
                }
        }

        public void submit(Runnable runnable) {
                if (shutdownInitiated)
                        throw new IllegalStateException("Thread pool is shutdown");
                if (runnable == null)
                        throw new NullPointerException("Task cannot be null");
                taskQueue.put(runnable);
        }

        public void shutdown() {
                shutdownInitiated = true;
                for (WorkerThread worker : workers)
                        worker.interrupt();
        }

        public List<Runnable> shutdownNow() {
                shutdownInitiated = true;
                List<Runnable> remainingTasks = new ArrayList<>();
                taskQueue.drainTo(remainingTasks);
                for (WorkerThread worker : workers)
                        worker.interrupt();
                return remainingTasks;
        }

        public boolean isShutdown() {
                return shutdownInitiated;
        }

        private class WorkerThread extends Thread {
                @Override
                public void run() {
                        while (!shutdownInitiated || !taskQueue.isEmpty()) {
                                try {
                                        Runnable task = taskQueue.take();
                                        task.run();
                                } catch (InterruptedException e) {
                                        if (shutdownInitiated) {
                                                Thread.currentThread().interrupt();
                                                break;
                                        }
                                }
                        }
                }
        }
}
