package server.game.utils;

public class ModifiedCyclicBarrier {

        private final int maxWaiters;
        private int waiters;
        private int passedWaiters;

        private boolean releaseLock = false;
        private final Runnable runnable;

        public ModifiedCyclicBarrier(int maxWaiters, Runnable runnable) {
                this.maxWaiters = maxWaiters;
                this.runnable = runnable;
        }

        public synchronized int await() throws InterruptedException {
                try {
                        waiters++;
                        while (waiters < maxWaiters && !releaseLock)
                                wait();
                        if (passedWaiters++ == 0 && !releaseLock)
                                notifyAll();
                        return releaseLock ? 1 : 2;
                } finally {
                        if (passedWaiters == maxWaiters) {
                                passedWaiters = 0;
                                waiters -= maxWaiters;
                                runnable.run();
                        }
                }
        }

        public synchronized void forceRelease() {
                if (waiters >= maxWaiters)
                        return;
                releaseLock = true;
                notifyAll();
        }

}
