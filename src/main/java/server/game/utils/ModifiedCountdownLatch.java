package server.game.utils;

import utils.Timer;

public class ModifiedCountdownLatch {

    public static final int NO_TIMER = -1;

    private final int bonusFactor;
    private final int bonusCount;
    private final int waitPeriod;
    private final int totalPlayers;

    private int answeredCount;
    private Timer timer;
    private boolean timerEnded = false;

    public ModifiedCountdownLatch(int bonusFactor, int bonusCount, int waitPeriod, int totalPlayers) {
        this.bonusFactor = bonusFactor;
        this.bonusCount = bonusCount;
        this.waitPeriod = waitPeriod;
        this.totalPlayers = totalPlayers;
    }

    public synchronized int countdown() {
        if (timerEnded || answeredCount >= totalPlayers)
            return 0;

        answeredCount++;

        int factor = (answeredCount <= bonusCount) ? bonusFactor : 1;

        if (answeredCount >= totalPlayers) {
            cancelTimer();
            notifyAll();
        }

        return factor;
    }

    public synchronized void await() throws InterruptedException {
        if (waitPeriod > 0 && timer == null) {
            timer = new Timer(waitPeriod, () -> {
                synchronized (this) {
                    timerEnded = true;
                    notifyAll();
                }
            });
            timer.start();
        }

        while (answeredCount < totalPlayers && !timerEnded)
            wait();
        cancelTimer();
    }

    private void cancelTimer() {
        if (timer != null)
            timer.interrupt();
    }

}
