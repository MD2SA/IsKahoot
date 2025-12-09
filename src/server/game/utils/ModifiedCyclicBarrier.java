package server.game.utils;

public class ModifiedCyclicBarrier {

        private final int maxWaiters;
        private int waiters;
        private boolean releaseLock = false;

        public ModifiedCyclicBarrier(int maxWaiters) {
                this.maxWaiters = maxWaiters;
        }

        public synchronized int await() throws InterruptedException {
                if (releaseLock)
                        return 1;
                waiters++;
                System.out.println(waiters + "/" + maxWaiters + " waiters on cyclic");
                if (waiters == maxWaiters)
                        notifyAll();
                while (waiters < maxWaiters && !releaseLock)
                        wait();
                return releaseLock ? 1 : 2;
        }

        public synchronized void forceRelease() {
                releaseLock = waiters < maxWaiters;
                notifyAll();
        }
}
