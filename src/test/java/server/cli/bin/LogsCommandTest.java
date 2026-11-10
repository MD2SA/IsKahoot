package server.cli.bin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutils.OutputCapture.capture;

import java.io.StringReader;
import java.util.Scanner;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import server.Server;
import server.cli.CommandContext;
import server.utils.Logger;
import testutils.OutputCapture.Captured;
import testutils.ServerTestSupport;

class LogsCommandTest {

    private static Logger logger;

    @BeforeAll
    static void initializeServerSingletonWithoutConsoleInput() {
        Server server = ServerTestSupport.initializeWithoutConsole();
        logger = server.getLogger();
    }

    @BeforeEach
    void clearLogs() {
        logger.clear();
    }

    @Test
    void exposesItsNameAndDescription() {
        LogsCommand command = new LogsCommand();

        assertEquals("logs", command.getName());
        assertEquals("Shows server logs.", command.getDescription());
    }

    @Test
    void listsEveryLogAndPrintsATipWhenCalledWithoutOptions() throws Exception {
        seedLogs();

        Captured<Void> output = execute(new LogsCommand());

        assertTrue(output.output().contains("ALL LOGS"));
        assertTrue(output.output().contains("debug message"));
        assertTrue(output.output().contains("info message"));
        assertTrue(output.output().contains("warn message"));
        assertTrue(output.output().contains("error message"));
        assertTrue(output.output().contains("TIP: Type 'logs -h' to see all available options."));
    }

    @Test
    void printsTheTipOnlyEveryThirdCall() throws Exception {
        LogsCommand command = new LogsCommand();

        String first = execute(command).output();
        String second = execute(command).output();
        String third = execute(command).output();
        String fourth = execute(command).output();

        assertTrue(first.contains("TIP:"));
        assertFalse(second.contains("TIP:"));
        assertFalse(third.contains("TIP:"));
        assertTrue(fourth.contains("TIP:"));
    }

    @Test
    void allOptionsListEveryLog() throws Exception {
        seedLogs();

        Captured<Void> shortOption = execute(new LogsCommand(), "-a");
        Captured<Void> longOption = execute(new LogsCommand(), "--all");

        assertTrue(shortOption.output().contains("ALL LOGS"));
        assertTrue(shortOption.output().contains("debug message"));
        assertTrue(longOption.output().contains("ALL LOGS"));
        assertTrue(longOption.output().contains("error message"));
    }

    @Test
    void filtersLogsByLevel() throws Exception {
        seedLogs();

        Captured<Void> debug = execute(new LogsCommand(), "-d");
        Captured<Void> info = execute(new LogsCommand(), "--info");
        Captured<Void> errors = execute(new LogsCommand(), "-e");

        assertTrue(debug.output().contains("DEBUG LOGS"));
        assertTrue(debug.output().contains("debug message"));
        assertFalse(debug.output().contains("info message"));

        assertTrue(info.output().contains("INFO LOGS"));
        assertTrue(info.output().contains("info message"));
        assertFalse(info.output().contains("debug message"));

        assertTrue(errors.output().contains("WARNINGS + ERRORS"));
        assertTrue(errors.output().contains("warn message"));
        assertTrue(errors.output().contains("error message"));
        assertFalse(errors.output().contains("info message"));
    }

    @Test
    void clearOptionRemovesEveryLog() throws Exception {
        seedLogs();

        Captured<Void> output = execute(new LogsCommand(), "-c");

        assertTrue(output.output().contains("Logs cleared."));
        assertTrue(logger.getAll().isEmpty());
    }

    @Test
    void helpOptionShowsTheUsage() throws Exception {
        Captured<Void> shortOption = execute(new LogsCommand(), "-h");
        Captured<Void> longOption = execute(new LogsCommand(), "--help");
        Captured<Void> questionMark = execute(new LogsCommand(), "?");

        assertTrue(shortOption.output().contains("USAGE"));
        assertTrue(shortOption.output().contains("logs [option]"));
        assertTrue(longOption.output().contains("USAGE"));
        assertTrue(questionMark.output().contains("USAGE"));
    }

    @Test
    void reportsAnUnknownOption() throws Exception {
        Captured<Void> output = execute(new LogsCommand(), "-x");

        assertTrue(output.output().contains("Unknown option: -x (use -h for help)"));
    }

    private static void seedLogs() {
        logger.debug("debug message");
        logger.info("info message");
        logger.warn("warn message");
        logger.error("error message");
    }

    private static Captured<Void> execute(LogsCommand command, String... args) {
        try (Scanner scanner = new Scanner(new StringReader(""))) {
            return capture(() -> {
                command.execute(scanner, new CommandContext(null), args);
                return null;
            });
        }
    }
}
