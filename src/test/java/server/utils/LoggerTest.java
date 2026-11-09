package server.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import server.utils.Logger.Level;
import server.utils.Logger.Log;

class LoggerTest {

    @Test
    void storesLogsWithTheirLevelAndMessage() {
        Logger logger = new Logger();

        logger.debug("debug message");
        logger.info("info message");
        logger.warn("warn message");
        logger.error("error message");

        List<Log> logs = logger.getAll();
        assertEquals(4, logs.size());
        assertEquals(List.of(Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR),
                logs.stream().map(log -> log.level).toList());
        assertEquals("info message", logs.get(1).message);
        assertNotNull(logs.get(0).timestamp);
    }

    @Test
    void filtersLogsByLevel() {
        Logger logger = new Logger();
        logger.debug("debug");
        logger.info("info");
        logger.warn("warn");
        logger.error("error");

        assertEquals(List.of("debug"), messages(logger.getDebug()));
        assertEquals(List.of("info"), messages(logger.getInfo()));
        assertEquals(List.of("warn", "error"), messages(logger.getErrors()));
    }

    @Test
    void clearRemovesEveryLog() {
        Logger logger = new Logger();
        logger.info("one");
        logger.warn("two");

        logger.clear();

        assertTrue(logger.getAll().isEmpty());
        assertTrue(logger.getErrors().isEmpty());
    }

    @Test
    void keepsOnlyTheMostRecentHundredLogs() {
        Logger logger = new Logger();

        for (int index = 0; index < 105; index++)
            logger.info("log-" + index);

        List<Log> logs = logger.getAll();
        assertEquals(100, logs.size());
        assertEquals("log-5", logs.getFirst().message);
        assertEquals("log-104", logs.getLast().message);
    }

    @Test
    void formatsALogLineWithTimestampLevelAndMessage() {
        Logger logger = new Logger();
        logger.warn("careful");

        String text = logger.getAll().getFirst().toString();

        assertTrue(text.contains("["));
        assertTrue(text.contains("WARN"));
        assertTrue(text.contains("careful"));
        assertTrue(text.matches("\\[\\d{2}:\\d{2}:\\d{2}] WARN  careful"));
    }

    private static List<String> messages(List<Log> logs) {
        return logs.stream().map(log -> log.message).toList();
    }
}
