package utils.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.Test;

class QuizValueObjectsTest {

    @Test
    void answerExposesItsConnectionIdentityAndBonus() {
        Answer answer = new Answer("A", "alice", 2, 3);

        assertEquals("A", answer.getTeamId());
        assertEquals("alice", answer.getUsername());
        assertEquals(2, answer.getOption());
        assertEquals(3, answer.getRoundNumber());
        assertEquals(0, answer.getBonus());

        answer.setBonus(5);

        assertEquals(5, answer.getBonus());
    }

    @Test
    void reducedQuestionHidesTheCorrectAnswerButKeepsThePublicFields() throws Exception {
        Question question = loadQuiz().getQuestion(0);

        ReducedQuestion reduced = new ReducedQuestion(question, 4, 123_456L);

        assertEquals(question.getQuestion(), reduced.getQuestion());
        assertEquals(question.getPoints(), reduced.getPoints());
        assertTrue(reduced.isIndividual());
        assertEquals(question.getOptions(), reduced.getOptions());
        assertEquals(4, reduced.getRoundNumber());
        assertEquals(123_456L, reduced.getDeadlineEpochMillis());
        assertThrows(UnsupportedOperationException.class, () -> reduced.getOptions().add("another"));
    }

    @Test
    void scoreboardAccumulatesScoresAndSortsThem() {
        Scoreboard scoreboard = new Scoreboard(List.of("A", "B", "C"));
        RoundResult first = new RoundResult();
        first.increaseScore(10, "B");
        first.increaseScore(20, "A");
        first.increaseScore(20, "C");

        scoreboard.applyResult(first);
        scoreboard.applyResult(null);

        assertEquals(List.of(new Score("A", 20), new Score("C", 20), new Score("B", 10)), scoreboard.getScoreboard());
    }

    @Test
    void scoreboardStartsEveryRosterTeamAtZero() {
        Scoreboard scoreboard = new Scoreboard(List.of("A", "B"));

        List<Score> scores = scoreboard.getScoreboard();

        assertFalse(scores.isEmpty());
        assertTrue(scores.contains(new Score("A", 0)));
        assertTrue(scores.contains(new Score("B", 0)));
    }

    private static Quiz loadQuiz() throws IOException {
        try (InputStream input = QuizValueObjectsTest.class.getClassLoader().getResourceAsStream("quiz-fast.json")) {
            return QuizCreator.getFrom(input).get(0);
        }
    }
}
