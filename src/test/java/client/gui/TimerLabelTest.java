package client.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TimerLabelTest {

    @Test
    void roundsPartialSecondsUp() {
        assertEquals(1, TimerLabel.secondsRemaining(1_000L, 1L));
        assertEquals(1, TimerLabel.secondsRemaining(1_000L, 0L));
        assertEquals(2, TimerLabel.secondsRemaining(1_001L, 0L));
    }

    @Test
    void clampsExpiredDeadlinesToZero() {
        assertEquals(0, TimerLabel.secondsRemaining(1_000L, 1_000L));
        assertEquals(0, TimerLabel.secondsRemaining(1_000L, 5_000L));
    }

    @Test
    void roundsWholeSecondsCorrectly() {
        assertEquals(5, TimerLabel.secondsRemaining(5_000L, 0L));
        assertEquals(4, TimerLabel.secondsRemaining(5_000L, 1_000L));
        assertEquals(1, TimerLabel.secondsRemaining(5_000L, 4_001L));
    }
}
