package utils.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class BlockingQueueTest {

    @Test
    void putsAndTakesInFifoOrder() throws InterruptedException {
        BlockingQueue<String> queue = new BlockingQueue<>();
        queue.put("one");
        queue.put("two");
        queue.put("three");

        assertEquals("one", queue.take());
        assertEquals("two", queue.take());
        assertEquals("three", queue.take());
        assertTrue(queue.isEmpty());
    }

    @Test
    void tracksSizeAndEmptiness() {
        BlockingQueue<Integer> queue = new BlockingQueue<>();
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());

        queue.put(1);
        queue.put(2);

        assertFalse(queue.isEmpty());
        assertEquals(2, queue.size());
    }

    @Test
    void drainToEmptiesTheQueueAndPreservesOrder() {
        BlockingQueue<String> queue = new BlockingQueue<>();
        queue.put("a");
        queue.put("b");
        List<String> drained = new ArrayList<>();
        drained.add("prefix");

        queue.drainTo(drained);

        assertEquals(List.of("prefix", "a", "b"), drained);
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    void canPutAgainAfterDrain() throws InterruptedException {
        BlockingQueue<String> queue = new BlockingQueue<>();
        queue.put("old");
        queue.drainTo(new ArrayList<>());

        queue.put("new");

        assertEquals("new", queue.take());
    }

    @Test
    @Timeout(2)
    void takeBlocksUntilAnItemIsPut() throws Exception {
        BlockingQueue<String> queue = new BlockingQueue<>();
        CountDownLatch consumerStarted = new CountDownLatch(1);
        AtomicBoolean taken = new AtomicBoolean();
        Thread consumer = new Thread(() -> {
            consumerStarted.countDown();
            try {
                queue.take();
                taken.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        consumer.start();

        assertTrue(consumerStarted.await(1, TimeUnit.SECONDS));
        assertFalse(taken.get());

        queue.put("item");
        consumer.join(1_000);

        assertFalse(consumer.isAlive());
        assertTrue(taken.get());
    }

    @Test
    @Timeout(2)
    void takeIsInterruptible() throws Exception {
        BlockingQueue<String> queue = new BlockingQueue<>();
        CountDownLatch consumerStarted = new CountDownLatch(1);
        AtomicBoolean interrupted = new AtomicBoolean();
        Thread consumer = new Thread(() -> {
            consumerStarted.countDown();
            try {
                queue.take();
            } catch (InterruptedException e) {
                interrupted.set(true);
            }
        });
        consumer.start();

        assertTrue(consumerStarted.await(1, TimeUnit.SECONDS));
        consumer.interrupt();
        consumer.join(1_000);

        assertFalse(consumer.isAlive());
        assertTrue(interrupted.get());
    }
}
