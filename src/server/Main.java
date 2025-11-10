package server;
import server.TUI.TUI;
import utils.Quiz;
import utils.QuizCreator;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {

        public static void main(String[] args) throws InterruptedException, IOException {

                List<Quiz> quizzes = QuizCreator.getFrom(new File("quizzes.json"));

                int[] gameInfo = TUI.gatherGameInfo(quizzes);


        }
}
