package server.cli;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutils.OutputCapture.capture;

import java.io.StringReader;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

import server.GameManager;
import testutils.OutputCapture.Captured;

class CommandDispatcherTest {

    @Test
    void dispatchesCommandsCaseInsensitivelyWithTrimmedArguments() throws Exception {
        GameManager manager = new GameManager();
        CommandContext context = new CommandContext(manager);
        CommandDispatcher dispatcher = new CommandDispatcher(context);
        RecordingCommand command = new RecordingCommand("echo");
        dispatcher.register(command);

        try (Scanner scanner = new Scanner(new StringReader(""))) {
            dispatcher.processLine(scanner, "  ECHO   one  two ");
            assertSame(scanner, command.scanner);
        }

        assertSame(context, command.context);
        assertEquals(2, command.args.length);
        assertArrayEquals(new String[]{"one", "two"}, command.args);
    }

    @Test
    void ignoresNullAndBlankLines() throws Exception {
        CommandDispatcher dispatcher = new CommandDispatcher(new CommandContext(new GameManager()));
        String output;

        try (Scanner scanner = new Scanner(new StringReader(""))) {
            Captured<Void> ignored = capture(() -> {
                dispatcher.processLine(scanner, null);
                dispatcher.processLine(scanner, "");
                dispatcher.processLine(scanner, "   ");
                return null;
            });
            output = ignored.output();
        }

        assertEquals("", output);
    }

    @Test
    void reportsUnknownCommands() throws Exception {
        CommandDispatcher dispatcher = new CommandDispatcher(new CommandContext(new GameManager()));

        Captured<Void> ignored;
        try (Scanner scanner = new Scanner(new StringReader(""))) {
            ignored = capture(() -> {
                dispatcher.processLine(scanner, "  NOPE  extra ");
                return null;
            });
        }

        assertTrue(ignored.output().contains("Unknown command: nope. Type 'help' for available commands."));
    }

    @Test
    void helpListsTheBuiltInCommands() throws Exception {
        CommandDispatcher dispatcher = new CommandDispatcher(new CommandContext(new GameManager()));

        Captured<Void> ignored = capture(() -> {
            dispatcher.showHelp();
            return null;
        });

        assertTrue(ignored.output().contains("Available commands"));
        assertTrue(ignored.output().contains("list"));
        assertTrue(ignored.output().contains("new"));
        assertTrue(ignored.output().contains("logs"));
        assertTrue(ignored.output().contains("help"));
        assertTrue(ignored.output().contains("exit"));
        assertTrue(ignored.output().contains("clear"));
    }

    private static final class RecordingCommand implements Command {

        private final String name;
        private Scanner scanner;
        private CommandContext context;
        private String[] args;

        RecordingCommand(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return "Records the invocation for assertions";
        }

        @Override
        public void execute(Scanner scanner, CommandContext context, String... args) {
            this.scanner = scanner;
            this.context = context;
            this.args = args.clone();
        }
    }
}
