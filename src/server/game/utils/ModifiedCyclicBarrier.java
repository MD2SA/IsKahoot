package server.game.utils;

public class ModifiedCyclicBarrier {

        private final int maxWaiters;
        private int waiters;
        private boolean release = false;

        public ModifiedCyclicBarrier(int maxWaiters) {
                this.maxWaiters = maxWaiters;
        }

        public synchronized int await() throws InterruptedException {
                waiters++;
                System.out.println("Waiters: " + waiters + "/" + maxWaiters);
                while (waiters < maxWaiters || release)
                        wait();
                notifyAll();
                return release ? 2 : 1;
        }

        public void forceRelease() {
                release = true;
                notifyAll();
        }
}
