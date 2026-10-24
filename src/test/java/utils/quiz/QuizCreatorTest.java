package utils.quiz;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        @Test
        void loadsBundledQuizzes() throws IOException {
                try (InputStream input = getClass().getClassLoader().getResourceAsStream("quizzes.json")) {
                        assertNotNull(input, "quizzes.json should be packaged as a resource");

                        List<Quiz> quizzes = QuizCreator.getFrom(input);

                        assertEquals(2, quizzes.size());
                        assertEquals("PCD - 1", quizzes.get(0).getName());
                        assertTrue(quizzes.get(0).getNumberOfQuestions() > 0);
                }
        }

        @Test
        void everyQuestionHasAnAnswerWithinItsOptions() throws IOException {
                try (InputStream input = getClass().getClassLoader().getResourceAsStream("quizzes.json")) {
                        assertNotNull(input);

                        for (Quiz quiz : QuizCreator.getFrom(input)) {
                                for (int i = 0; i < quiz.getNumberOfQuestions(); i++) {
                                        Question question = quiz.getQuestion(i);
                                        assertNotNull(question.getQuestion());
                                        assertTrue(question.getCorrect() >= 0
                                                        && question.getCorrect() < question.getOptions().size(),
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
