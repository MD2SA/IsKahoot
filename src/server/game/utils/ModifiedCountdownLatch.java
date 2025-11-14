package server.game.utils;

public class ModifiedCountdownLatch {

        private final int bonusFactor;
        private int bonusCount;
        private int waitPeriod;
        private int maxWaiters;
        private int curWaiters;
        private boolean timerStarted;
        private boolean timerEnded;

        public ModifiedCountdownLatch(int bonusFactor, int bonusCount, int waitPeriod, int maxWaiters) {
                this.bonusFactor = bonusFactor;
                this.bonusCount = bonusCount;
                this.waitPeriod = waitPeriod;
                this.maxWaiters = maxWaiters;
        }

        public synchronized int countdown() {
                int factor = curWaiters<bonusCount?bonusFactor:1;
                curWaiters++;
                return factor;
        }

        public synchronized void await() throws InterruptedException {
                if( timerStarted ) return;
                timerStarted = true;
                new Timer(waitPeriod, new Runnable() {
                        @Override
                        public void run() {
                                timerEnded = true;
                        }
                }).start();
                while(maxWaiters < curWaiters || timerEnded)
                        wait();
                curWaiters = 0;
        }

}
