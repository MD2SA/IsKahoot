package server.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        try (RoundManager manager = new RoundManager(loadQuiz(), teams)) {
            assertEquals(AnswerStatus.REJECTED, manager.answer("alice", new Answer("A", "alice", 1, 0)));

            assertTrue(teams.registerPlayer("alice", "A"));
            manager.prepareNextRound();
            manager.openRound();
            assertEquals(AnswerStatus.ROUND_COMPLETED, manager.answer("alice", new Answer("A", "alice", 0, 0)));

            Scoreboard scoreboard = manager.awaitResult();
            assertEquals(List.of(new Score("A", 0)), scoreboard.getScoreboard());
        }
    }

    @Test
    void startsTimerWithADeadlineForTheCurrentRound() throws Exception {
        TeamManager teams = new TeamManager(1, 1);
        assertTrue(teams.registerPlayer("alice", "A"));
        try (RoundManager manager = new RoundManager(loadQuiz(), teams)) {
            manager.prepareNextRound();
            manager.openRound();

            long deadline = manager.startTimer();

            assertTrue(deadline > System.currentTimeMillis());
        }
    }

    @Test
    void applyingTheSameRoundResultTwiceDoesNotDoubleTheScore() throws Exception {
        TeamManager teams = new TeamManager(1, 1);
        assertTrue(teams.registerPlayer("alice", "A"));
        try (RoundManager manager = new RoundManager(loadQuiz(), teams)) {
            manager.prepareNextRound();
            manager.openRound();
            assertEquals(AnswerStatus.ROUND_COMPLETED, manager.answer("alice", new Answer("A", "alice", 1, 0)));

            assertEquals(List.of(new Score("A", 10)), manager.awaitResult().getScoreboard());
            assertEquals(List.of(new Score("A", 10)), manager.awaitResult().getScoreboard());
        }
    }

    private static Quiz loadQuiz() throws IOException {
        try (InputStream input = RoundManagerTest.class.getClassLoader().getResourceAsStream("quiz-fast.json")) {
            return QuizCreator.getFrom(input).get(0);
        }
    }
}
