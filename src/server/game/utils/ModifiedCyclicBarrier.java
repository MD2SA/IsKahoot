package server.game.utils;

public class ModifiedCyclicBarrier {

        private final int maxWaiters;
        private int waiters;
        private int passedWaiters;

        private boolean releaseLock = false;

        public ModifiedCyclicBarrier(int maxWaiters) {
                this.maxWaiters = maxWaiters;
        }

        public synchronized int await() throws InterruptedException {
                waiters++;
                while (waiters < maxWaiters && !releaseLock)
                        wait();
                if (passedWaiters++ == 0 && !releaseLock)
                        notifyAll();
                if (passedWaiters == maxWaiters) {
                        passedWaiters = 0;
                        waiters -= maxWaiters;
                }
                return releaseLock ? 1 : 2;
        }

        public synchronized void forceRelease() {
                if (waiters >= maxWaiters)
                        return;
                releaseLock = true;
                notifyAll();
        }

}
