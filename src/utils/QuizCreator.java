package utils;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class QuizCreator {

        private List<Quiz> quizzes;

        public static List<Quiz> getFrom(File file) throws IOException {
                Gson gson = new Gson();
                try( FileReader reader = new FileReader(file)) {
                        QuizCreator creator = gson.fromJson(reader, QuizCreator.class);
                        return creator!=null ? creator.quizzes : new ArrayList<>();
                }
        }
}
