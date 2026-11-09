package client.gui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public final class TimerLabel extends JLabel {

    private static final long serialVersionUID = 1L;
    private static final int UPDATE_INTERVAL_MILLIS = 100;
    private static final int WARNING_SECONDS = 5;

    private transient Timer timer;
    private long deadlineEpochMillis;

    public TimerLabel() {
        super("", SwingConstants.CENTER);
        setFont(new Font("SansSerif", Font.BOLD, 36));
        setForeground(Color.BLACK);
    }

    public void start(long deadlineEpochMillis) {
        stop();

        this.deadlineEpochMillis = deadlineEpochMillis;
        updateDisplay();

        timer = new Timer(UPDATE_INTERVAL_MILLIS, event -> updateDisplay());
        timer.start();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    private void updateDisplay() {
        int secondsLeft = secondsRemaining(deadlineEpochMillis, System.currentTimeMillis());
        setText(String.valueOf(secondsLeft));
        setForeground(secondsLeft <= WARNING_SECONDS ? Color.RED : Color.BLACK);

        if (secondsLeft == 0)
            stop();
    }

    static int secondsRemaining(long deadlineEpochMillis, long nowEpochMillis) {
        long remainingMillis = Math.max(0L, deadlineEpochMillis - nowEpochMillis);
        return (int) ((remainingMillis + 999L) / 1_000L);
    }
}
