package server.TUI.command.impl;

import java.util.List;
import java.util.Scanner;

import server.GameManager;
import server.game.GameState;
import utils.quiz.Quiz;

import server.TUI.command.Command;
import server.TUI.command.CommandContext;

import server.TUI.utils.*;

public class CreateGameCommand implements Command {

        @Override
        public String getName() {
                return "create";
        }

        @Override
        public String getDescription() {
                return "Creates a new game (interactive mode)";
        }

        @Override
        public void execute(Scanner scanner, CommandContext context) {
                GameManager gameManager = context.gameManager();
                List<Quiz> quizzes = gameManager.getQuizzes();

                System.out.println(Constants.MARGIN);
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
                System.out.println(Constants.MARGIN);

                gameManager.addNewGame(new GameState(Helpers.generateUniqueRandomCode(4), teamCapacity, numberOfTeams,
                                selectedQuiz));
        }
}
