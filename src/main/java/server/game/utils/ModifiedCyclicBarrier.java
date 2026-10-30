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
            release();
            return;
        }

        while (!released)
            wait();

        return;

    }

    public synchronized void forceRelease() {
        if (!released)
            release();
    }

    private void release() {
        released = true;
        try {
            if (barrierAction != null)
                barrierAction.run();
        } finally {
            notifyAll();
        }
    }
}
