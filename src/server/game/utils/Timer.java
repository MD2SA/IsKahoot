package server.game.utils;

public class Timer extends Thread {

        private int waitTime;
        private final Runnable callback;

        private boolean hasEnded;

        public Timer(int waitTime) {
                this(waitTime, null);
        }

        public Timer(int waitTime, Runnable callback) {
                this.waitTime = waitTime;
                this.callback = callback;
        }

        @Override
        public void run() {
                while (!interrupted() && waitTime-- > 0) {
                        try {
                                sleep(1000);
                        } catch (InterruptedException e) {
                        }
                }

                if (callback != null)
                        callback.run();
                hasEnded = true;
        }

        public boolean hasEnded() {
                return hasEnded;
        }

        public Timer restartTimer() {
                return new Timer(waitTime, callback);
        }

}
