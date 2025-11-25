package server;

import server.TUI.command.CommandDispatcher;
import server.TUI.command.CommandContext;
import server.game.GameState;
import utils.quiz.Quiz;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class GameManager extends Thread {

        private final List<Quiz> quizzes;
        private final Scanner scanner = new Scanner(System.in);
        private final CommandDispatcher dispatcher;

        private final Map<String, GameState> games = new HashMap<>();

        public GameManager(List<Quiz> quizzes) {
                this.quizzes = quizzes;
                this.dispatcher = new CommandDispatcher(new CommandContext(this));
        }

        public List<Quiz> getQuizzes() {
                return quizzes;
        }

        public void addNewGame(GameState gameState) {
                if (gameState == null)
                        return;
                games.put(gameState.getCode(), gameState);
        }

        public List<GameState> getGames() {
                return new ArrayList<>(games.values());
        }

        @Override
        public void run() {
                System.out.println("Server console ready. Type 'help' for commands.\n");
                while (true) {
                        System.out.print("> ");
                        String line = scanner.nextLine();
                        dispatcher.processLine(scanner, line);
                        // GameInfo gameInfo = TUI.gatherGameInfo(quizzes);
                        // GameState gameState = new GameState(gameInfo);
                }
        }

}
