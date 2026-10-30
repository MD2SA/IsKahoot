package server.game.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class ModifiedCyclicBarrierTest {

    @Test
    @Timeout(5)
    void releasesEveryWaiterAndRunsTheActionOnce() throws Exception {
        AtomicInteger actions = new AtomicInteger();
        ModifiedCyclicBarrier barrier = new ModifiedCyclicBarrier(3, actions::incrementAndGet);
        ExecutorService pool = Executors.newFixedThreadPool(3);

        try {
            CountDownLatch start = new CountDownLatch(1);
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < 3; i++) {
                futures.add(pool.submit(() -> {
                    start.await();
                    barrier.await();
                    return null;
                }));
            }

            start.countDown();

            for (Future<?> future : futures)
                future.get(2, TimeUnit.SECONDS);

            assertEquals(1, actions.get());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    @Timeout(5)
    void forceReleaseUnblocksWaitersAndRunsTheAction() throws Exception {
        AtomicInteger actions = new AtomicInteger();
        ModifiedCyclicBarrier barrier = new ModifiedCyclicBarrier(2, actions::incrementAndGet);
        ExecutorService pool = Executors.newSingleThreadExecutor();

        try {
            CountDownLatch started = new CountDownLatch(1);
            Future<?> waiter = pool.submit(() -> {
                started.countDown();
                barrier.await();
                return null;
            });

            assertTrue(started.await(2, TimeUnit.SECONDS));
            barrier.forceRelease();
            waiter.get(2, TimeUnit.SECONDS);

            assertEquals(1, actions.get());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    @Timeout(5)
    void releasesWaitersWhenTheActionThrows() throws Exception {
        ModifiedCyclicBarrier barrier = new ModifiedCyclicBarrier(2, () -> {
            throw new IllegalStateException("barrier action failed");
        });
        ExecutorService pool = Executors.newFixedThreadPool(2);

        try {
            CountDownLatch start = new CountDownLatch(1);
            Future<?> first = pool.submit(() -> {
                start.await();
                barrier.await();
                return null;
            });
            Future<?> second = pool.submit(() -> {
                start.await();
                barrier.await();
                return null;
            });

            start.countDown();

            int failures = 0;
            for (Future<?> future : List.of(first, second)) {
                try {
                    future.get(2, TimeUnit.SECONDS);
                } catch (ExecutionException e) {
                    failures++;
                }
            }

            assertEquals(1, failures);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    @Timeout(5)
    void doesNotRunTheActionTwice() throws Exception {
        AtomicInteger actions = new AtomicInteger();
        ModifiedCyclicBarrier barrier = new ModifiedCyclicBarrier(2, actions::incrementAndGet);

        barrier.forceRelease();
        barrier.await();
        barrier.forceRelease();

        assertEquals(1, actions.get());
    }
}
