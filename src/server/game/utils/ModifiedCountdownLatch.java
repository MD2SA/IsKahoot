package server.game.utils;

public class ModifiedCountdownLatch {

        public static final int NO_TIMER = -1;

        private final int bonusFactor;
        private int bonusCount;
        private int waitPeriod;
        private int maxWaiters;
        private int curWaiters;
        private boolean timerEnded;

        public ModifiedCountdownLatch(int bonusFactor, int bonusCount, int waitPeriod, int maxWaiters) {
                this.bonusFactor = bonusFactor;
                this.bonusCount = bonusCount;
                this.waitPeriod = waitPeriod;
                this.maxWaiters = maxWaiters;
        }

        public synchronized int countdown() {
                int factor = curWaiters < bonusCount ? bonusFactor : 1;
                curWaiters++;
                return factor;
        }

        public synchronized void await() throws InterruptedException {
                if (waitPeriod != -1)
                        new Timer(waitPeriod, () -> {
                                timerEnded = true;
                        });
                while (curWaiters < maxWaiters || (waitPeriod != -1 && timerEnded))
                        wait();
                curWaiters = 0;
                timerEnded = false;
        }

}
