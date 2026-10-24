package server.cli.bin;

import java.util.List;
import java.util.Scanner;

import configs.Config;
import server.GameManager;
import utils.quiz.Quiz;

import server.cli.Command;
import server.cli.CommandContext;

import server.cli.utils.*;

public class CreateGameCommand implements Command {

        private static final int GAME_ID_LENGTH = 4;

        @Override
        public String getName() {
                return Config.Server.CREATE_GAME_COMMAND;
        }

        @Override
        public String getDescription() {
                return "Creates a new game (interactive mode)";
        }

        @Override
        public void execute(Scanner scanner, CommandContext context, String[] args) {
                GameManager gameManager = context.gameManager();
                List<Quiz> quizzes = gameManager.getQuizzes();

                System.out.println();
                Helpers.printSectionHeader("CREATING A NEW GAME");

                int argIndex = 0;

                int numberOfTeams = Helpers.getArgOrReadPositiveInt(args, argIndex++, scanner,
                                Constants.NUMBER_OF_TEAMS);

                int teamCapacity = Helpers.getArgOrReadPositiveInt(args, argIndex++, scanner,
                                Constants.NUMBER_OF_PLAYERS_PER_TEAM);

                Helpers.printSectionHeader("Available quizzes:");
                for (int i = 0; i < quizzes.size(); i++)
                        System.out.printf("%d. %s%n", i + 1, quizzes.get(i).getName());

                int quizNumber = Helpers.getArgOrReadIntInRange(args, argIndex++, scanner, 1, quizzes.size(),
                                Constants.QUIZ_NUMBER) - 1;

                String id = Helpers.getArgOrReadGameCode(args, argIndex, scanner, gameManager, GAME_ID_LENGTH);

                System.out.printf("\nCreating a game with %d teams of %d players with the quiz %d\n\n",
                                numberOfTeams,
                                teamCapacity, quizNumber+1);

                gameManager.createGame(id, numberOfTeams, teamCapacity, quizNumber);

                Helpers.printSectionHeader("CREATED GAME WITH CODE - \"" + id + "\"");
                System.out.println(Constants.MARGIN);
        }
}
