package server.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import utils.quiz.Answer;
import utils.quiz.Question;
import utils.quiz.Quiz;
import utils.quiz.QuizCreator;
import utils.quiz.RoundResult;

class RoundStateTest {

    private static final Map<String, String> ROSTER = Map.of("alice", "A", "bob", "A");

    @Test
    void completesWhenEveryPlayerHasAnswered() throws Exception {
        RoundState round = new RoundState(0, individualQuestion(), ROSTER);
        round.open();

        assertEquals(AnswerStatus.ACCEPTED, round.submitAnswer("alice", new Answer("A", "alice", 1, 0)));
        assertEquals(AnswerStatus.ROUND_COMPLETED, round.submitAnswer("bob", new Answer("A", "bob", 1, 0)));

        RoundResult result = round.awaitResult();
        assertEquals(20, result.getResult().get("A"));
    }

    @Test
    void rejectsAnswersFromAnotherTeam() throws Exception {
        RoundState round = new RoundState(0, individualQuestion(), Map.of("alice", "A"));
        round.open();

        assertEquals(AnswerStatus.REJECTED, round.submitAnswer("alice", new Answer("B", "alice", 1, 0)));
        assertEquals(AnswerStatus.REJECTED, round.submitAnswer("alice", new Answer("A", "alice", 99, 0)));
        assertEquals(AnswerStatus.ROUND_COMPLETED, round.submitAnswer("alice", new Answer("A", "alice", 1, 0)));
    }

    @Test
    void timeoutCompletesWithTheAnswersReceivedSoFar() throws Exception {
        RoundState round = new RoundState(0, individualQuestion(), ROSTER);
        round.open();

        assertEquals(AnswerStatus.ACCEPTED, round.submitAnswer("alice", new Answer("A", "alice", 1, 0)));

        round.timeout();

        RoundResult result = round.awaitResult();
        assertEquals(10, result.getResult().get("A"));
    }

    @Test
    void ignoresAnswersAfterTimeout() throws Exception {
        RoundState round = new RoundState(0, individualQuestion(), ROSTER);
        round.open();

        round.timeout();

        assertEquals(AnswerStatus.REJECTED, round.submitAnswer("alice", new Answer("A", "alice", 1, 0)));
    }

    @Test
    void departureCompletesRoundAndCountsMissingTeamAnswerAsWrong() throws Exception {
        RoundState round = new RoundState(1, teamQuestion(), ROSTER);
        round.open();

        assertEquals(AnswerStatus.ACCEPTED, round.submitAnswer("alice", new Answer("A", "alice", 2, 1)));
        assertTrue(round.playerLeft("bob"));
        assertEquals(AnswerStatus.REJECTED, round.submitAnswer("bob", new Answer("A", "bob", 2, 1)));

        RoundResult result = round.awaitResult();
        assertEquals(10, result.getResult().get("A"));
    }

    @Test
    void departuresBeforeOpenAreAccountedForWhenTheRoundOpens() throws Exception {
        RoundState round = new RoundState(0, individualQuestion(), ROSTER);

        assertFalse(round.playerLeft("alice"));
        assertFalse(round.playerLeft("bob"));
        round.open();

        assertTrue(round.awaitResult().getResult().isEmpty());
    }

    @Test
    void openRejectsRoundsThatAreNotPrepared() throws Exception {
        RoundState round = new RoundState(0, individualQuestion(), ROSTER);
        round.open();

        assertThrows(IllegalStateException.class, round::open);
    }

    @Test
    void isOpenReflectsTheRoundLifecycle() throws Exception {
        RoundState round = new RoundState(0, individualQuestion(), ROSTER);

        assertFalse(round.isOpen());
        round.open();
        assertTrue(round.isOpen());
        round.timeout();
        assertFalse(round.isOpen());
    }

    @Test
    void timeoutBeforeOpenIsIgnored() throws Exception {
        RoundState round = new RoundState(0, individualQuestion(), ROSTER);

        round.timeout();

        assertFalse(round.isOpen());
        round.open();
        assertEquals(AnswerStatus.ACCEPTED, round.submitAnswer("alice", new Answer("A", "alice", 1, 0)));
    }

    @Test
    void departureOfTheLastPlayerCompletesTheRound() throws Exception {
        RoundState round = new RoundState(0, individualQuestion(), Map.of("alice", "A"));
        round.open();

        assertTrue(round.playerLeft("alice"));

        assertTrue(round.awaitResult().getResult().isEmpty());
    }

    @Test
    void scoringFailureIsReportedThroughTheResultFuture() throws Exception {
        RoundState round = new RoundState(0, failingQuestion(), Map.of("alice", "A"));
        round.open();

        assertEquals(AnswerStatus.ROUND_COMPLETED, round.submitAnswer("alice", new Answer("A", "alice", 0, 0)));

        assertThrows(ExecutionException.class, round::awaitResult);
    }

    private static Question individualQuestion() throws IOException {
        try (InputStream input = RoundStateTest.class.getClassLoader().getResourceAsStream("quiz-fast.json")) {
            Quiz quiz = QuizCreator.getFrom(input).get(0);
            return quiz.getQuestion(0);
        }
    }

    private static Question teamQuestion() throws IOException {
        try (InputStream input = RoundStateTest.class.getClassLoader().getResourceAsStream("quiz-fast.json")) {
            Quiz quiz = QuizCreator.getFrom(input).get(0);
            return quiz.getQuestion(1);
        }
    }

    private static Question failingQuestion() {
        return new Question() {
            @Override
            public List<String> getOptions() {
                return List.of("only option");
            }

            @Override
            public boolean isCorrect(int option) {
                throw new IllegalStateException("scoring failed");
            }
        };
    }
}
