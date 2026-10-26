package server.game.utils;

public class ModifiedCyclicBarrier {

    private final int maxWaiters;
    private int waiters;

    private boolean released = false;
    private final Runnable barrierAction;

    public ModifiedCyclicBarrier(int maxWaiters, Runnable barrierAction) {
        this.maxWaiters = maxWaiters;
        this.barrierAction = barrierAction;
    }

    public synchronized void await() throws InterruptedException {
        if (released)
            return;

        waiters++;

        if (waiters >= maxWaiters) {
            released = true;
            if (barrierAction != null) {
                barrierAction.run();
            }
            notifyAll();
            return;
        }

        while (!released)
            wait();

        return;

    }

    public synchronized void forceRelease() {
        if (!released) {
            released = true;
            if (barrierAction != null) {
                barrierAction.run();
            }
            notifyAll();
        }
    }
}
