package server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static testutils.OutputCapture.capture;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import server.game.AnswerStatus;
import server.game.GameSession;
import testutils.OutputCapture.Captured;
import utils.quiz.Answer;
import utils.quiz.Quiz;
import utils.quiz.QuizCreator;
import utils.quiz.ReducedQuestion;
import utils.quiz.Score;
import utils.quiz.Scoreboard;
import utils.tools.ConcurrentMap;

@SuppressWarnings({"PMD.CloseResource", "PMD.ExcessiveImports"}) // The recording client is a resource-free test double.
class GameManagerTest {

    @Test
    void exposesTheQuizCatalogueAndStartsWithoutGames() throws Exception {
        GameManager manager = new GameManager();
        try {
            List<Quiz> quizzes = manager.getQuizzes();

            assertFalse(quizzes.isEmpty());
            assertThrows(UnsupportedOperationException.class, () -> quizzes.add(null));
            assertFalse(manager.hasGame("missing"));
            assertTrue(manager.getWaitingGames().isEmpty());
            assertTrue(manager.getActiveGames().isEmpty());
            assertTrue(manager.getAllGames().isEmpty());
            assertNull(manager.registerToGame(new RecordingClient("alice", "A"), "missing"));
        } finally {
            manager.shutdown();
        }
    }

    @Test
    @Timeout(10)
    void movesAFullGameToActiveAndRemovesItWhenTheGameEnds() throws Exception {
        GameManager manager = new GameManager();
        try {
            GameSession session = new GameSession("game-1", loadQuiz(), 1, 1);
            waitingGames(manager).put("game-1", session);
            RecordingClient alice = new RecordingClient("alice", "A");

            assertSame(session, manager.registerToGame(alice, "game-1"));

            ReducedQuestion first = alice.pollQuestion(2, TimeUnit.SECONDS);
            assertNotNull(first);
            assertEquals(0, first.getRoundNumber());
            assertTrue(manager.hasGame("game-1"));
            assertTrue(manager.getWaitingGames().isEmpty());
            assertEquals(List.of(session), manager.getActiveGames());

            assertEquals(AnswerStatus.ROUND_COMPLETED, session.receiveAnswer("alice", new Answer("A", "alice", 1, 0)));
            assertEquals(List.of(new Score("A", 10)), alice.pollScoreboard(2, TimeUnit.SECONDS));

            ReducedQuestion second = alice.pollQuestion(2, TimeUnit.SECONDS);
            assertNotNull(second);
            assertEquals(1, second.getRoundNumber());

            assertEquals(AnswerStatus.ROUND_COMPLETED, session.receiveAnswer("alice", new Answer("A", "alice", 2, 1)));
            assertEquals(List.of(new Score("A", 30)), alice.pollScoreboard(2, TimeUnit.SECONDS));

            assertTrue(alice.awaitEnd(2, TimeUnit.SECONDS));
            waitUntil(() -> !manager.hasGame("game-1"));
            assertTrue(manager.getActiveGames().isEmpty());
        } finally {
            manager.shutdown();
        }
    }

    @Test
    void runsTheConsoleLoopUntilInputEnds() throws Exception {
        GameManager manager = new GameManager();
        InputStream original = System.in;

        try (InputStream input = new ByteArrayInputStream("help\n".getBytes(StandardCharsets.UTF_8))) {
            System.setIn(input);

            Captured<Void> output = capture(() -> {
                try {
                    manager.run();
                } catch (NoSuchElementException expectedEndOfInput) {
                    // The console loop ends when stdin is exhausted.
                    assertNotNull(expectedEndOfInput);
                }
                return null;
            });

            assertTrue(output.output().contains("Server console ready"));
            assertTrue(output.output().contains("Available commands"));
        } finally {
            System.setIn(original);
            manager.shutdown();
        }
    }

    @SuppressWarnings({"unchecked", "PMD.AvoidAccessibilityAlteration"}) // Seeds the private registry.
    private static ConcurrentMap<String, GameSession> waitingGames(GameManager manager)
            throws ReflectiveOperationException {
        Field field = GameManager.class.getDeclaredField("waitingGames");
        field.setAccessible(true);
        return (ConcurrentMap<String, GameSession>) field.get(manager);
    }

    private static void waitUntil(BooleanSupplier condition) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline)
            Thread.onSpinWait();
        assertTrue(condition.getAsBoolean(), "condition was not met before the timeout");
    }

    private static Quiz loadQuiz() throws IOException {
        try (InputStream input = GameManagerTest.class.getClassLoader().getResourceAsStream("quiz-fast.json")) {
            return QuizCreator.getFrom(input).get(0);
        }
    }

    private static final class RecordingClient extends DealWithClient {

        private final String clientId;
        private final String teamId;
        private final BlockingQueue<ReducedQuestion> questions = new LinkedBlockingQueue<>();
        private final BlockingQueue<List<Score>> scoreboards = new LinkedBlockingQueue<>();
        private final CountDownLatch end = new CountDownLatch(1);

        RecordingClient(String clientId, String teamId) {
            super(null, null);
            this.clientId = clientId;
            this.teamId = teamId;
        }

        @Override
        public String getClientId() {
            return clientId;
        }

        @Override
        public String getTeamId() {
            return teamId;
        }

        @Override
        public void sendQuestion(ReducedQuestion question) {
            questions.add(question);
        }

        @Override
        public void sendScoreboard(Scoreboard scoreboard) {
            scoreboards.add(scoreboard.getScoreboard());
        }

        @Override
        public void sendEndGame() {
            end.countDown();
        }

        ReducedQuestion pollQuestion(long timeout, TimeUnit unit) throws InterruptedException {
            return questions.poll(timeout, unit);
        }

        List<Score> pollScoreboard(long timeout, TimeUnit unit) throws InterruptedException {
            return scoreboards.poll(timeout, unit);
        }

        boolean awaitEnd(long timeout, TimeUnit unit) throws InterruptedException {
            return end.await(timeout, unit);
        }
    }
}
