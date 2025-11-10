package server.game.utils;

public class ModifiedCountdownLatch {

        private final int bonusFactor;
        private int bonusCount;
        private int waitPeriod;
        private int maxWaiters;
        private int curWaiters;
        private boolean timerStarted;

        public ModifiedCountdownLatch(int bonusFactor, int bonusCount, int waitPeriod, int maxWaiters) {
                this.bonusFactor = bonusFactor;
                this.bonusCount = bonusCount;
                this.maxWaiters = maxWaiters;
        }

        public synchronized void startTimer() {
                if( timerStarted ) return;
                new Thread(()->{
                        int timer = waitPeriod;
                        while(timer > 0) {
                                timer--;
                                try {
                                        Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                }
                        }
                        curWaiters = 0;
                } ).start();
                timerStarted = true;
        }

        public synchronized int countdown() {
                int factor = curWaiters<bonusCount?bonusFactor:1;
                curWaiters++;
                while(maxWaiters < curWaiters) {
                        try { wait(); } catch (InterruptedException e) { }
                }
                curWaiters = 0;
                return factor;
        }

        public synchronized void await() throws InterruptedException {
        }

        public boolean hasTimerStarted() {
                return timerStarted;
        }
}
