package utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class RoundTimerTest {

    @Test
    void rejectsInvalidArguments() {
        try (RoundTimer timer = new RoundTimer()) {
            assertThrows(IllegalArgumentException.class, () -> timer.schedule(-1, () -> {
            }));
            assertThrows(IllegalArgumentException.class, () -> timer.schedule(0, null));
        }
    }

    @Test
    @Timeout(3)
    void runsCallbackAfterZeroSeconds() throws Exception {
        try (RoundTimer timer = new RoundTimer()) {
            CountDownLatch fired = new CountDownLatch(1);

            timer.schedule(0, fired::countDown);

            assertTrue(fired.await(2, TimeUnit.SECONDS));
        }
    }

    @Test
    @Timeout(3)
    void cancelPreventsTheCallback() throws Exception {
        try (RoundTimer timer = new RoundTimer()) {
            CountDownLatch fired = new CountDownLatch(1);

            timer.schedule(1, fired::countDown);
            timer.cancel();

            assertFalse(fired.await(200, TimeUnit.MILLISECONDS));
        }
    }

    @Test
    @Timeout(3)
    @SuppressWarnings("PMD.CloseResource") // The timer is closed explicitly to test close().
    void closePreventsTheCallbackAndRejectsNewSchedules() throws Exception {
        RoundTimer timer = new RoundTimer();
        CountDownLatch fired = new CountDownLatch(1);

        timer.schedule(1, fired::countDown);
        timer.close();

        assertFalse(fired.await(200, TimeUnit.MILLISECONDS));
        assertThrows(IllegalStateException.class, () -> timer.schedule(0, () -> {
        }));
    }

    @Test
    @Timeout(3)
    void aLaterScheduleReplacesTheEarlierOne() throws Exception {
        try (RoundTimer timer = new RoundTimer()) {
            CountDownLatch first = new CountDownLatch(1);
            CountDownLatch second = new CountDownLatch(1);

            timer.schedule(1, first::countDown);
            timer.schedule(0, second::countDown);

            assertTrue(second.await(2, TimeUnit.SECONDS));
            assertFalse(first.await(200, TimeUnit.MILLISECONDS));
        }
    }

    @Test
    @Timeout(3)
    void callbackFailureDoesNotStopTheTimer() throws Exception {
        Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            // The timer reports callback failures to the uncaught exception handler.
        });

        try (RoundTimer timer = new RoundTimer()) {
            CountDownLatch failed = new CountDownLatch(1);
            CountDownLatch next = new CountDownLatch(1);

            timer.schedule(0, () -> {
                failed.countDown();
                throw new IllegalStateException("boom");
            });
            assertTrue(failed.await(2, TimeUnit.SECONDS));

            timer.schedule(0, next::countDown);

            assertTrue(next.await(2, TimeUnit.SECONDS));
        } finally {
            Thread.setDefaultUncaughtExceptionHandler(previous);
        }
    }
}
