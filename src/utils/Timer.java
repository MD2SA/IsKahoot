package utils;

public class Timer extends Thread {

        private int waitTime;
        private final Runnable callback;
        private final boolean everyTime;

        private boolean hasEnded;

        public Timer(int waitTime) {
                this(waitTime, null, false);
        }

        public Timer(int waitTime, Runnable callback) {
                this(waitTime, callback, false);
        }

        public Timer(int waitTime, Runnable callback, boolean everyTime) {
                this.waitTime = waitTime;
                this.callback = callback;
                this.everyTime = everyTime;
        }

        @Override
        public void run() {
                try {
                        while (!interrupted() && waitTime > 0) {
                                Thread.sleep(1000);
                                waitTime--;
                                if (everyTime && callback != null) {
                                        callback.run();
                                }
                        }
                        if (!everyTime && callback != null) {
                                callback.run();
                        }
                } catch (InterruptedException e) {
                } finally {
                        hasEnded = true;
                }
        }

        public boolean hasEnded() {
                return hasEnded;
        }

        public int getTimeLeft() {
                return waitTime;
        }

}
