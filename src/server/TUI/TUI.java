package server.TUI;

import server.TUI.utils.Constants;
import server.TUI.utils.Helpers;
import utils.quiz.Quiz;

import java.util.List;
import java.util.Scanner;

public class TUI {

        /**
         * Collects basic game configuration from the user.
         *
         * <p>
         * This method displays a header, asks for the number of teams,
         * the number of players per team, and then shows the list of available
         * quizzes so the user can choose one. All inputs are validated through
         * helper methods.
         * </p>
         *
         * @param quizzes the list of quizzes available for selection
         * @return an array containing:
         *         <ul>
         *              <li>index 0: number of teams</li>
         *              <li>index 1: number of players per team</li>
         *              <li>index 2: index of the selected quiz in the quizzes list</li>
         *         </ul>
         */
        public static int[] gatherGameInfo(List<Quiz> quizzes) {
                Scanner scanner = new Scanner(System.in);

                System.out.println(Constants.HEADER);

                int numberOfTeams = Helpers.readPositiveInt(scanner, Constants.NUMBER_OF_TEAMS);
                int teamCapacity = Helpers.readPositiveInt(scanner, Constants.NUMBER_OF_PLAYERS_PER_TEAM);

                System.out.println("Available quizzes:");
                for (int i = 0; i < quizzes.size(); i++) {
                        System.out.printf("%d. %s%n", i + 1, quizzes.get(i).getName());
                }

                int quizNumber = Helpers.readIntInRange(scanner, 1, quizzes.size(), Constants.QUIZ_NUMBER) - 1;

                Quiz selectedQuiz = quizzes.get(quizNumber);

                System.out.printf("%nCreating a game with %d teams of %d players with the quiz \"%s\"%n", numberOfTeams,
                                teamCapacity, selectedQuiz.getName());
                return new int[] { numberOfTeams, teamCapacity, quizNumber };
        }

}
