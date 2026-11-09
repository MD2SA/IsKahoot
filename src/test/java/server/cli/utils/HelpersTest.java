package server.cli.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutils.OutputCapture.capture;

import java.io.StringReader;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

import server.GameManager;
import testutils.OutputCapture.Captured;

class HelpersTest {

    @Test
    void readPositiveIntRetriesUntilTheInputIsPositive() throws Exception {
        try (Scanner scanner = new Scanner(new StringReader("abc\n-3\n7\n"))) {
            Captured<Integer> read = capture(() -> Helpers.readPositiveInt(scanner, "Teams:"));

            assertEquals(7, read.value());
            assertTrue(read.output().contains("Teams:"));
            assertTrue(read.output().contains("Invalid input. Please enter a number."));
            assertTrue(read.output().contains("Please enter a positive number."));
        }
    }

    @Test
    void readIntInRangeRetriesUntilTheInputIsInRange() throws Exception {
        try (Scanner scanner = new Scanner(new StringReader("0\nabc\n3\n"))) {
            Captured<Integer> read = capture(() -> Helpers.readIntInRange(scanner, 1, 4, "Quiz:"));

            assertEquals(3, read.value());
            assertTrue(read.output().contains("Invalid input. Please enter a number."));
            assertTrue(read.output().contains("Please enter a number between 1 and 4."));
        }
    }

    @Test
    void getArgOrReadPositiveIntUsesAValidArgument() throws Exception {
        try (Scanner scanner = new Scanner(new StringReader(""))) {
            Captured<Integer> read = capture(
                    () -> Helpers.getArgOrReadPositiveInt(new String[]{"5"}, 0, scanner, "Teams:"));

            assertEquals(5, read.value());
            assertTrue(read.output().contains("Teams: 5"));
        }
    }

    @Test
    void getArgOrReadPositiveIntFallsBackOnAnInvalidArgument() throws Exception {
        try (Scanner scanner = new Scanner(new StringReader("4\n"))) {
            Captured<Integer> read = capture(
                    () -> Helpers.getArgOrReadPositiveInt(new String[]{"x"}, 0, scanner, "Teams:"));

            assertEquals(4, read.value());
            assertTrue(read.output().contains("Invalid argument 'x'. Falling back to input."));
        }
    }

    @Test
    void getArgOrReadIntInRangeUsesAValidArgument() throws Exception {
        try (Scanner scanner = new Scanner(new StringReader(""))) {
            Captured<Integer> read = capture(
                    () -> Helpers.getArgOrReadIntInRange(new String[]{"2"}, 0, scanner, 1, 3, "Quiz:"));

            assertEquals(2, read.value());
            assertTrue(read.output().contains("Quiz: 2"));
        }
    }

    @Test
    void getArgOrReadIntInRangeFallsBackWhenTheArgumentIsOutOfRange() throws Exception {
        try (Scanner scanner = new Scanner(new StringReader("2\n"))) {
            Captured<Integer> read = capture(
                    () -> Helpers.getArgOrReadIntInRange(new String[]{"9"}, 0, scanner, 1, 3, "Quiz:"));

            assertEquals(2, read.value());
            assertTrue(read.output().contains("Invalid argument '9'. Falling back to input."));
        }
    }

    @Test
    void readGameCodeNormalizesAPersonalizedCode() throws Exception {
        GameManager manager = new GameManager();
        try (Scanner scanner = new Scanner(new StringReader("abcd\n"))) {
            Captured<String> read = capture(() -> Helpers.readGameCode(scanner, manager, 4));

            assertEquals("ABCD", read.value());
            assertTrue(read.output().contains("Personalized code accepted: ABCD"));
        }
    }

    @Test
    void readGameCodeGeneratesARandomCodeForBlankInput() throws Exception {
        GameManager manager = new GameManager();
        try (Scanner scanner = new Scanner(new StringReader("\n"))) {
            Captured<String> read = capture(() -> Helpers.readGameCode(scanner, manager, 4));

            assertTrue(read.value().matches("[A-Z0-9]{4}"));
            assertTrue(read.output().contains("Random code generated: " + read.value()));
        }
    }

    @Test
    void readGameCodeFallsBackToARandomCodeAfterTwoInvalidAttempts() throws Exception {
        GameManager manager = new GameManager();
        try (Scanner scanner = new Scanner(new StringReader("bad!\nstill bad\n"))) {
            Captured<String> read = capture(() -> Helpers.readGameCode(scanner, manager, 4));

            assertTrue(read.value().matches("[A-Z0-9]{4}"));
            assertTrue(read.output().contains("Only letters and numbers are allowed."));
            assertTrue(read.output().contains("Too many invalid attempts. A random code has been generated: "));
        }
    }

    @Test
    void generateUniqueRandomCodeUsesTheRequestedLengthAndAlphabet() throws Exception {
        GameManager manager = new GameManager();

        String code = Helpers.generateUniqueRandomCode(8, manager);

        assertEquals(8, code.length());
        assertTrue(code.matches("[A-Z0-9]{8}"));
    }

    @Test
    void getArgOrReadGameCodeUsesAValidArgument() throws Exception {
        GameManager manager = new GameManager();
        try (Scanner scanner = new Scanner(new StringReader(""))) {
            Captured<String> read = capture(
                    () -> Helpers.getArgOrReadGameCode(new String[]{"xy12"}, 0, scanner, manager, 4));

            assertEquals("XY12", read.value());
            assertTrue(read.output().contains("Game code (letters/numbers only) - press enter for random: XY12"));
        }
    }

    @Test
    void getArgOrReadGameCodeFallsBackOnAnInvalidArgument() throws Exception {
        GameManager manager = new GameManager();
        try (Scanner scanner = new Scanner(new StringReader("ok1\n"))) {
            Captured<String> read = capture(
                    () -> Helpers.getArgOrReadGameCode(new String[]{"bad!"}, 0, scanner, manager, 4));

            assertEquals("OK1", read.value());
            assertTrue(read.output().contains("Invalid or duplicate code in arguments. Falling back to input."));
        }
    }

}
