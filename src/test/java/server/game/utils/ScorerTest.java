package server.game.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import utils.quiz.Answer;
import utils.quiz.Question;
import utils.quiz.Quiz;
import utils.quiz.QuizCreator;
import utils.quiz.RoundResult;

class ScorerTest {

    private static final Map<String, String> ROSTER = Map.of("alice", "A", "bob", "A", "carol", "B");

    @Test
    void individualBonusUsesAnswerPositionNotCorrectness() throws Exception {
        Quiz quiz = loadQuiz();
        Question question = quiz.getQuestion(0);

        Map<String, Answer> answers = new LinkedHashMap<>();
        answers.put("alice", new Answer("A", "alice", 1, 0));
        answers.put("bob", new Answer("A", "bob", 0, 0));
        answers.put("carol", new Answer("B", "carol", 1, 0));

        RoundResult result = Scorer.score(question, ROSTER, answers);

        assertEquals(10, result.getResult().get("A"));
        assertEquals(5, result.getResult().get("B"));
    }

    @Test
    void teamDoublesWhenEveryMemberIsCorrect() throws Exception {
        Quiz quiz = loadQuiz();
        Question question = quiz.getQuestion(1);

        Map<String, Answer> answers = new LinkedHashMap<>();
        answers.put("alice", new Answer("A", "alice", 2, 1));
        answers.put("bob", new Answer("A", "bob", 2, 1));

        RoundResult result = Scorer.score(question, ROSTER, answers);

        assertEquals(20, result.getResult().get("A"));
    }

    @Test
    void teamScoresBestCorrectAnswerWhenSomeoneIsWrong() throws Exception {
        Quiz quiz = loadQuiz();
        Question question = quiz.getQuestion(1);

        Map<String, Answer> answers = new LinkedHashMap<>();
        answers.put("alice", new Answer("A", "alice", 2, 1));
        answers.put("bob", new Answer("A", "bob", 0, 1));

        RoundResult result = Scorer.score(question, ROSTER, answers);

        assertEquals(10, result.getResult().get("A"));
    }

    @Test
    void missingAnswerCountsAsWrong() throws Exception {
        Quiz quiz = loadQuiz();
        Question question = quiz.getQuestion(1);

        Map<String, Answer> answers = Map.of("alice", new Answer("A", "alice", 2, 1));

        RoundResult result = Scorer.score(question, ROSTER, answers);

        assertEquals(10, result.getResult().get("A"));
    }

    @Test
    void teamWithoutAnswersScoresNothing() throws Exception {
        Quiz quiz = loadQuiz();
        Question question = quiz.getQuestion(1);

        RoundResult result = Scorer.score(question, ROSTER, Map.of());

        assertTrue(result.getResult().isEmpty());
    }

    private static Quiz loadQuiz() throws IOException {
        try (InputStream input = ScorerTest.class.getClassLoader().getResourceAsStream("quiz-fast.json")) {
            return QuizCreator.getFrom(input).get(0);
        }
    }
}
