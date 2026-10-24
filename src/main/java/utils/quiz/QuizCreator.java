package utils.quiz;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.gson.Gson;

public class QuizCreator {

        private List<Quiz> quizzes;

        public static List<Quiz> getFrom(InputStream input) throws IOException {
                try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                        QuizCreator creator = new Gson().fromJson(reader, QuizCreator.class);
                        if (creator == null || creator.quizzes == null)
                                throw new IOException("The quiz file does not contain a valid 'quizzes' list");
                        return creator.quizzes;
                }
        }
}
