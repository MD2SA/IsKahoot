package utils.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

class QuizCreatorTest {

    private static final String SAMPLE_QUIZ = """
            {
              "quizzes": [
                {
                  "name": "Sample quiz",
                  "questions": [
                    {
                      "question": "What is 2 + 2?",
                      "points": 5,
                      "correct": 1,
                      "isIndividual": true,
                      "options": ["3", "4"]
                    }
                  ]
                }
              ]
            }
            """;

    @Test
    void bundledQuizFileIsPackaged() throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("quizzes.json")) {
            assertNotNull(input, "quizzes.json should be packaged as a resource");

            List<Quiz> quizzes = QuizCreator.getFrom(input);

            assertNotNull(quizzes);
            assertFalse(quizzes.isEmpty(), "The server needs at least one quiz to create games");
        }
    }

    @Test
    void parsesAQuizFromAnInlineFixture() throws IOException {
        InputStream input = new ByteArrayInputStream(SAMPLE_QUIZ.getBytes(StandardCharsets.UTF_8));

        List<Quiz> quizzes = QuizCreator.getFrom(input);

        assertEquals(1, quizzes.size());

        Quiz quiz = quizzes.get(0);
        assertEquals("Sample quiz", quiz.getName());
        assertEquals(1, quiz.getNumberOfQuestions());

        Question question = quiz.getQuestion(0);
        assertEquals("What is 2 + 2?", question.getQuestion());
        assertEquals(5, question.getPoints());
        assertEquals(1, question.getCorrect());
        assertTrue(question.isIndividual());
        assertEquals(List.of("3", "4"), question.getOptions());
        assertTrue(question.isCorrect(1));
        assertFalse(question.isCorrect(0));
    }

    @Test
    void everyBundledQuestionHasAValidAnswer() throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("quizzes.json")) {
            assertNotNull(input);

            for (Quiz quiz : QuizCreator.getFrom(input)) {
                for (int i = 0; i < quiz.getNumberOfQuestions(); i++) {
                    Question question = quiz.getQuestion(i);
                    assertNotNull(question.getQuestion());
                    assertNotNull(question.getOptions());
                    assertTrue(question.getCorrect() >= 0 && question.getCorrect() < question.getOptions().size(),
                            "Correct index out of range in quiz '" + quiz.getName() + "'");
                }
            }
        }
    }

    @Test
    void rejectsAFileWithoutAQuizList() {
        InputStream input = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
        assertThrows(IOException.class, () -> QuizCreator.getFrom(input));
    }
}
