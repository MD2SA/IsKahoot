package server.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import server.DealWithClient;
import utils.quiz.Answer;
import utils.quiz.Quiz;
import utils.quiz.QuizCreator;
import utils.quiz.ReducedQuestion;
import utils.quiz.Score;
import utils.quiz.Scoreboard;

@SuppressWarnings("PMD.CloseResource") // The recording clients are resource-free test doubles.
class GameSessionTest {

    @Test
    @Timeout(10)
    void playsEveryRoundAndBroadcastsQuestionsScoreboardsAndEnd() throws Exception {
        GameSession session = new GameSession("game-1", loadQuiz(), 1, 2);
        RecordingClient alice = new RecordingClient("alice", "A");
        RecordingClient bob = new RecordingClient("bob", "A");

        assertTrue(session.join(alice));
        assertTrue(session.join(bob));
        assertTrue(session.isFull());
        assertEquals("game-1", session.getId());

        Thread game = new Thread(session, "game-session");
        game.start();

        ReducedQuestion first = alice.questions.poll(2, TimeUnit.SECONDS);
        assertNotNull(first);
        assertEquals(0, first.getRoundNumber());
        assertEquals(4, first.getOptions().size());

        assertEquals(AnswerStatus.ACCEPTED, session.receiveAnswer("alice", new Answer("A", "alice", 1, 0)));
        assertEquals(AnswerStatus.ROUND_COMPLETED, session.receiveAnswer("bob", new Answer("A", "bob", 0, 0)));

        assertEquals(List.of(new Score("A", 10)), alice.scoreboards.poll(2, TimeUnit.SECONDS));

        ReducedQuestion second = alice.questions.poll(2, TimeUnit.SECONDS);
        assertNotNull(second);
        assertEquals(1, second.getRoundNumber());

        assertEquals(AnswerStatus.ACCEPTED, session.receiveAnswer("alice", new Answer("A", "alice", 2, 1)));
        assertEquals(AnswerStatus.ROUND_COMPLETED, session.receiveAnswer("bob", new Answer("A", "bob", 2, 1)));

        assertEquals(List.of(new Score("A", 30)), alice.scoreboards.poll(2, TimeUnit.SECONDS));

        game.join(2_000);

        assertFalse(game.isAlive());
        assertEquals(1, alice.endGames);
        assertEquals(1, bob.endGames);
        assertTrue(alice.questions.isEmpty());
        assertTrue(alice.scoreboards.isEmpty());
    }

    @Test
    @Timeout(10)
    void rejectsPlayersWhenFullAndALeavingPlayerFreesASeat() throws Exception {
        GameSession session = new GameSession("game-2", loadQuiz(), 1, 2);
        RecordingClient alice = new RecordingClient("alice", "A");
        RecordingClient bob = new RecordingClient("bob", "A");
        RecordingClient carol = new RecordingClient("carol", "A");

        assertTrue(session.join(alice));
        assertTrue(session.join(bob));
        assertFalse(session.join(carol), "Team A is already full");

        assertTrue(session.leave(bob));
        assertTrue(session.join(carol));
        assertTrue(session.isFull());
    }

    @Test
    void rejectsAnswersBeforeTheFirstRound() throws Exception {
        GameSession session = new GameSession("game-3", loadQuiz(), 1, 1);

        assertEquals(AnswerStatus.REJECTED, session.receiveAnswer("alice", new Answer("A", "alice", 0, 0)));
    }

    private static Quiz loadQuiz() throws IOException {
        try (InputStream input = GameSessionTest.class.getClassLoader().getResourceAsStream("quiz-fast.json")) {
            return QuizCreator.getFrom(input).get(0);
        }
    }

    private static final class RecordingClient extends DealWithClient {

        private final String clientId;
        private final String teamId;
        private final BlockingQueue<ReducedQuestion> questions = new LinkedBlockingQueue<>();
        private final BlockingQueue<List<Score>> scoreboards = new LinkedBlockingQueue<>();

        private int endGames;

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
            endGames++;
        }
    }
}
