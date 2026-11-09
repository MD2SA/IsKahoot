package server.cli.bin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutils.OutputCapture.capture;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.io.StringReader;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

import server.GameManager;
import server.cli.CommandContext;
import server.cli.CommandDispatcher;
import server.game.GameSession;
import testutils.OutputCapture.Captured;
import utils.quiz.Quiz;
import utils.quiz.QuizCreator;
import utils.tools.ConcurrentMap;

@SuppressWarnings("PMD.AvoidAccessibilityAlteration") // Seeds the private game registry for the list command.
class CliCommandsTest {

    @Test
    void listGamesReportsWhenThereAreNoGames() throws Exception {
        GameManager manager = new GameManager();
        try (Scanner scanner = new Scanner(new StringReader(""))) {
            Captured<Void> output = capture(() -> {
                new ListGamesCommand().execute(scanner, new CommandContext(manager));
                return null;
            });

            assertTrue(output.output().contains("Active games:"));
            assertTrue(output.output().contains("(no games)"));
        }
    }

    @Test
    void listGamesPrintsEveryGameId() throws Exception {
        GameManager manager = new GameManager();
        waitingGames(manager).put("game-1", new GameSession("game-1", loadQuiz(), 1, 1));
        try (Scanner scanner = new Scanner(new StringReader(""))) {
            Captured<Void> output = capture(() -> {
                new ListGamesCommand().execute(scanner, new CommandContext(manager));
                return null;
            });

            assertTrue(output.output().contains("1. game-1"));
        }
    }

    @Test
    void helpCommandDelegatesToTheDispatcher() throws Exception {
        GameManager manager = new GameManager();
        try (Scanner scanner = new Scanner(new StringReader(""))) {
            CommandDispatcher dispatcher = new CommandDispatcher(new CommandContext(manager));
            HelpCommand command = new HelpCommand(dispatcher);

            Captured<Void> output = capture(() -> {
                command.execute(scanner, new CommandContext(manager));
                return null;
            });

            assertEquals("help", command.getName());
            assertEquals("Provides help", command.getDescription());
            assertTrue(output.output().contains("Available commands"));
            assertTrue(output.output().contains("list"));
        }
    }

    @Test
    void clearCommandClearsTheConsole() throws Exception {
        GameManager manager = new GameManager();
        try (Scanner scanner = new Scanner(new StringReader(""))) {
            ClearCommand command = new ClearCommand();

            Captured<Void> output = capture(() -> {
                command.execute(scanner, new CommandContext(manager));
                return null;
            });

            assertEquals("clear", command.getName());
            assertTrue(output.output().contains("\033[H\033[2J"));
        }
    }

    @SuppressWarnings("unchecked")
    private static ConcurrentMap<String, GameSession> waitingGames(GameManager manager)
            throws ReflectiveOperationException {
        Field field = GameManager.class.getDeclaredField("waitingGames");
        field.setAccessible(true);
        return (ConcurrentMap<String, GameSession>) field.get(manager);
    }

    private static Quiz loadQuiz() throws IOException {
        try (InputStream input = CliCommandsTest.class.getClassLoader().getResourceAsStream("quiz-fast.json")) {
            return QuizCreator.getFrom(input).get(0);
        }
    }
}
