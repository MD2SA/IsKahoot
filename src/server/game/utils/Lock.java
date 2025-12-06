package server.game.utils;

public class Lock {

        private boolean locked = false;

        public synchronized void lock() throws InterruptedException {
                while (locked)
                        wait();
                locked = true;
        }

        public synchronized void unlock() {
                locked = false;
                notify();
        }

}
