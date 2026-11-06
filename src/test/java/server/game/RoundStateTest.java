package server.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

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

        assertFalse(round.submitAnswer("alice", new Answer("A", "alice", 1, 0)));
        assertTrue(round.submitAnswer("bob", new Answer("A", "bob", 1, 0)));

        RoundResult result = round.awaitResult();
        assertEquals(20, result.getResult().get("A"));
    }

    @Test
    void rejectsAnswersFromAnotherTeam() throws Exception {
        RoundState round = new RoundState(0, individualQuestion(), Map.of("alice", "A"));
        round.open();

        assertFalse(round.submitAnswer("alice", new Answer("B", "alice", 1, 0)));
        assertTrue(round.submitAnswer("alice", new Answer("A", "alice", 1, 0)));
    }

    @Test
    void timeoutCompletesWithTheAnswersReceivedSoFar() throws Exception {
        RoundState round = new RoundState(0, individualQuestion(), ROSTER);
        round.open();

        assertFalse(round.submitAnswer("alice", new Answer("A", "alice", 1, 0)));

        round.timeout();

        RoundResult result = round.awaitResult();
        assertEquals(10, result.getResult().get("A"));
    }

    @Test
    void ignoresAnswersAfterTimeout() throws Exception {
        RoundState round = new RoundState(0, individualQuestion(), ROSTER);
        round.open();

        round.timeout();

        assertFalse(round.submitAnswer("alice", new Answer("A", "alice", 1, 0)));
    }

    @Test
    void departureCompletesRoundAndCountsMissingTeamAnswerAsWrong() throws Exception {
        RoundState round = new RoundState(1, teamQuestion(), ROSTER);
        round.open();

        assertFalse(round.submitAnswer("alice", new Answer("A", "alice", 2, 1)));
        assertTrue(round.playerLeft("bob"));
        assertFalse(round.submitAnswer("bob", new Answer("A", "bob", 2, 1)));

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
}
