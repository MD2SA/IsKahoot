package server.game.utils;

import server.Server;

public class ModifiedCountdownLatch {

        public static final int NO_TIMER = -1;

        private final int bonusFactor;
        private final int bonusCount;
        private final int waitPeriod;
        private final int maxWaiters;

        private int curWaiters;
        private Timer timer;

        public ModifiedCountdownLatch(int bonusFactor, int bonusCount, int waitPeriod, int maxWaiters) {
                this.bonusFactor = bonusFactor;
                this.bonusCount = bonusCount;
                this.waitPeriod = waitPeriod;
                this.maxWaiters = maxWaiters;
        }

        public synchronized int countdown() {
                int factor = curWaiters < bonusCount ? bonusFactor : 1;
                curWaiters++;
                Server.getInstance().getLogger().debug(String.valueOf(curWaiters));
                if (curWaiters >= maxWaiters) {
                        Server.getInstance().getLogger().debug("NOTIFIYING");
                        notifyAll();
                }

                return factor;
        }

        public synchronized void await() throws InterruptedException {
                if (waitPeriod > 0 && timer == null) {
                        timer = new Timer(waitPeriod, () -> {
                                synchronized (this) {
                                        notifyAll();
                                }
                        });
                        timer.start();
                }

                while (curWaiters < maxWaiters && (timer == null || !timer.hasEnded()))
                        wait();

                Server.getInstance().getLogger().debug("No longer waiting!");

                timer = null;
                curWaiters = 0;
        }

}
