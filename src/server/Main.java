package server;
import utils.Quiz;
import utils.QuizCreator;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {

        public static void main(String[] args) throws InterruptedException, IOException {

                List<Quiz> quizzes = QuizCreator.getFrom(new File("quizzes.json"));

                for (Quiz quiz : quizzes) {
                        System.out.println(quiz.toString());
                }

        }
}
