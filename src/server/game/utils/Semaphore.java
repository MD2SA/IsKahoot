package server.game.utils;

public class Semaphore {

        private int permits;

        public Semaphore() {
                this(1);
        }

        public Semaphore(int permits) {
                if (permits <= 0)
                        throw new IllegalArgumentException("There must at least 1 permit");
                this.permits = permits;
        }

        public synchronized void lock() {

        }

}
