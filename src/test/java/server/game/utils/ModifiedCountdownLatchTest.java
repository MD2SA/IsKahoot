package server.game.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class ModifiedCountdownLatchTest {

    @Test
    void grantsTheBonusOnlyToTheFirstAnswers() {
        ModifiedCountdownLatch latch = new ModifiedCountdownLatch(2, 2, -1, 4);

        assertEquals(2, latch.countdown());
        assertEquals(2, latch.countdown());
        assertEquals(1, latch.countdown());
        assertEquals(1, latch.countdown());
        assertEquals(0, latch.countdown(), "Answers after every player answered score no bonus");
    }

    @Test
    @Timeout(5)
    void releasesWhenEveryPlayerHasAnswered() throws InterruptedException {
        ModifiedCountdownLatch latch = new ModifiedCountdownLatch(2, 1, -1, 3);

        latch.countdown();
        latch.countdown();
        latch.countdown();

        latch.await();
    }

    @Test
    @Timeout(5)
    void releasesWhenTheTimerExpires() throws InterruptedException {
        ModifiedCountdownLatch latch = new ModifiedCountdownLatch(2, 1, 1, 2);

        long start = System.nanoTime();
        latch.await();
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;

        assertTrue(elapsedMillis >= 900, "The latch waited only " + elapsedMillis + " ms");
    }
}
