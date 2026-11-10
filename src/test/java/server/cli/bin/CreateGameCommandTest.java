package server.cli.bin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutils.OutputCapture.capture;

import java.io.StringReader;
import java.util.Scanner;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import server.GameManager;
import server.cli.CommandContext;
import testutils.OutputCapture.Captured;
import testutils.ServerTestSupport;

class CreateGameCommandTest {

    @BeforeAll
    static void initializeServerSingletonWithoutConsoleInput() {
        ServerTestSupport.initializeWithoutConsole();
    }

    @Test
    void exposesItsNameAndDescription() {
        CreateGameCommand command = new CreateGameCommand();

        assertEquals("new", command.getName());
        assertEquals("Creates a new game (interactive mode)", command.getDescription());
    }

    @Test
    void createsAGameFromCommandArguments() throws Exception {
        GameManager manager = new GameManager();
        CreateGameCommand command = new CreateGameCommand();

        try (Scanner scanner = new Scanner(new StringReader(""))) {
            Captured<Void> output = capture(() -> {
                command.execute(scanner, new CommandContext(manager), "2", "3", "1", "ABCD");
                return null;
            });

            assertTrue(manager.hasGame("ABCD"));
            assertTrue(output.output().contains("CREATING A NEW GAME"));
            assertTrue(output.output().contains("Creating a game with 2 teams of 3 players with the quiz 1"));
            assertTrue(output.output().contains("CREATED GAME WITH CODE - \"ABCD\""));
        }
    }

    @Test
    void readsTheMissingValuesFromTheConsole() throws Exception {
        GameManager manager = new GameManager();
        CreateGameCommand command = new CreateGameCommand();

        try (Scanner scanner = new Scanner(new StringReader("4\n2\n1\nWXYZ\n"))) {
            Captured<Void> output = capture(() -> {
                command.execute(scanner, new CommandContext(manager));
                return null;
            });

            assertTrue(manager.hasGame("WXYZ"));
            assertFalse(manager.hasGame("ABCD"));
            assertTrue(output.output().contains("Creating a game with 4 teams of 2 players with the quiz 1"));
            assertTrue(output.output().contains("Personalized code accepted: WXYZ"));
        }
    }
}
