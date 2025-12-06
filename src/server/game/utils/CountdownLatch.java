package server.game.utils;

public class CountdownLatch {

        private final int totalPlayers;
        private int answeredCount;

        public CountdownLatch(int totalPlayers) {
                this.totalPlayers = totalPlayers;
        }

        public synchronized void countdown() {
                if (++answeredCount >= totalPlayers)
                        notifyAll();
        }

        public synchronized void await() throws InterruptedException {
                while (answeredCount < totalPlayers)
                        wait();
        }

}
