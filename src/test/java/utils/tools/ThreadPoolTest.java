package utils.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class ThreadPoolTest {

    @Test
    void rejectsNonPositivePoolSizes() {
        assertThrows(IllegalArgumentException.class, () -> new ThreadPool(0));
        assertThrows(IllegalArgumentException.class, () -> new ThreadPool(-1));
    }

    @Test
    void rejectsNullTasks() {
        ThreadPool pool = new ThreadPool(1);
        assertThrows(NullPointerException.class, () -> pool.submit(null));
        pool.shutdown();
    }

    @Test
    @Timeout(3)
    void runsSubmittedTasks() throws Exception {
        ThreadPool pool = new ThreadPool(2);
        CountDownLatch done = new CountDownLatch(3);
        AtomicInteger executions = new AtomicInteger();

        for (int task = 0; task < 3; task++) {
            pool.submit(() -> {
                executions.incrementAndGet();
                done.countDown();
            });
        }

        assertTrue(done.await(2, TimeUnit.SECONDS));
        assertEquals(3, executions.get());
        pool.shutdown();
    }

    @Test
    @Timeout(3)
    void rejectsSubmissionsAfterShutdown() {
        ThreadPool pool = new ThreadPool(1);

        pool.shutdown();

        assertTrue(pool.isShutdown());
        assertThrows(IllegalStateException.class, () -> pool.submit(() -> {
        }));
    }

    @Test
    @Timeout(3)
    void shutdownNowReturnsTasksThatWereStillQueued() throws Exception {
        ThreadPool pool = new ThreadPool(1);
        CountDownLatch running = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        pool.submit(() -> {
            running.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        assertTrue(running.await(1, TimeUnit.SECONDS));

        CountDownLatch queuedTaskRan = new CountDownLatch(1);
        pool.submit(queuedTaskRan::countDown);

        List<Runnable> remaining = pool.shutdownNow();

        assertEquals(1, remaining.size());
        assertEquals(1, queuedTaskRan.getCount());
        release.countDown();
    }

    @Test
    @Timeout(3)
    void aFailingTaskDoesNotPreventLaterTasks() throws Exception {
        Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            // The pool reports task failures to the uncaught exception handler.
        });

        ThreadPool pool = new ThreadPool(1);
        try {
            CountDownLatch ran = new CountDownLatch(1);
            pool.submit(() -> {
                throw new IllegalStateException("boom");
            });
            pool.submit(ran::countDown);

            assertTrue(ran.await(2, TimeUnit.SECONDS));
        } finally {
            pool.shutdown();
            Thread.setDefaultUncaughtExceptionHandler(previous);
        }
    }
}
