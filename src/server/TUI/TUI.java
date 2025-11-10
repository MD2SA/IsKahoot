package server.TUI;

import server.TUI.utils.Constants;
import server.TUI.utils.Helpers;
import utils.Quiz;

import java.util.List;
import java.util.Scanner;

public class TUI {

        public static int[] gatherGameInfo(List<Quiz> quizzes) {
                try (Scanner scanner = new Scanner(System.in)) {

                        System.out.println(Constants.HEADER);

                        int teams = Helpers.readPositiveInt(scanner, Constants.NUMBER_OF_TEAMS);
                        int playersPerTeam = Helpers.readPositiveInt(scanner, Constants.NUMBER_OF_PLAYERS_PER_TEAM);

                        System.out.println("Available quizzes:");
                        for (int i = 0; i < quizzes.size(); i++) {
                                System.out.printf("%d. %s%n", i + 1, quizzes.get(i).getName());
                        }

                        int quizNumber = Helpers.readIntInRange(scanner, 1, quizzes.size(), Constants.QUIZ_NUMBER) - 1;

                        Quiz selectedQuiz = quizzes.get(quizNumber);

                        System.out.printf("%nCreating a game with %d teams of %d players with the quiz \"%s\"%n", teams, playersPerTeam, selectedQuiz.getName());
                        return new int[]{teams, playersPerTeam, quizNumber};
                }
        }


}