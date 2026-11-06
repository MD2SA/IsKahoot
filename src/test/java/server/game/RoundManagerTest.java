package server.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.Test;

import utils.quiz.Answer;
import utils.quiz.Quiz;
import utils.quiz.QuizCreator;
import utils.quiz.Score;
import utils.quiz.Scoreboard;

class RoundManagerTest {

    @Test
    void rejectsAnswerBeforeFirstRoundAndIncludesZeroScoreTeams() throws Exception {
        TeamManager teams = new TeamManager(1, 1);
        RoundManager manager = new RoundManager(loadQuiz(), teams);
        try {
            assertDoesNotThrow(() -> manager.answer("alice", new Answer("A", "alice", 1, 0)));

            assertTrue(teams.registerPlayer("alice", "A"));
            manager.prepareNextRound();
            manager.openRound();
            manager.answer("alice", new Answer("A", "alice", 0, 0));

            Scoreboard scoreboard = manager.awaitResult();
            assertEquals(List.of(new Score("A", 0)), scoreboard.getScoreboard());
        } finally {
            manager.close();
        }
    }

    @Test
    void startsTimerWithADeadlineForTheCurrentRound() throws Exception {
        TeamManager teams = new TeamManager(1, 1);
        assertTrue(teams.registerPlayer("alice", "A"));
        RoundManager manager = new RoundManager(loadQuiz(), teams);
        try {
            manager.prepareNextRound();
            manager.openRound();

            long deadline = manager.startTimer();

            assertTrue(deadline > System.currentTimeMillis());
        } finally {
            manager.close();
        }
    }

    private static Quiz loadQuiz() throws IOException {
        try (InputStream input = RoundManagerTest.class.getClassLoader().getResourceAsStream("quiz-fast.json")) {
            return QuizCreator.getFrom(input).get(0);
        }
    }
}
