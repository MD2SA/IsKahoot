package server;

import server.cli.CommandDispatcher;
import server.cli.CommandContext;
import server.game.GameState;
import utils.quiz.Quiz;
import utils.quiz.QuizCreator;
import utils.tools.ConcurrentMap;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameManager extends Thread {

        private final List<Quiz> quizzes;
        private final ConcurrentMap<String, GameState> games = new ConcurrentMap<>();

        private final ExecutorService gameExecutor = Executors.newFixedThreadPool(5);
        private final CommandDispatcher dispatcher;

        public GameManager() throws IOException {
                this.quizzes = QuizCreator.getFrom(new File("quizzes.json"));
                this.dispatcher = new CommandDispatcher(new CommandContext(this));
        }

        public void createGame(String gameId, int teams, int capacity, int quizNumber) {
                Quiz quiz = quizzes.get(quizNumber);
                GameState game = new GameState(gameId, teams, capacity, quiz, this);
                games.put(gameId, game);
                Server.getInstance().getLogger().debug("Game created: " + gameId);
        }

        public void startGameWhenFull(String gameId) {
                GameState game = games.get(gameId);
                if (game != null && game.isFull() && !game.hasStarted())
                        gameExecutor.submit(game);
        }

        public void removeGame(String gameId) {
                games.remove(gameId);
        }

        @Override
        public void run() {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Server console ready. Type 'help' for commands.\n");
                while (true) {
                        System.out.print("> ");
                        String line = scanner.nextLine();
                        dispatcher.processLine(scanner, line);
                }
        }

        public List<Quiz> getQuizzes() {
                return quizzes;
        }

        public GameState getGame(String gameId) {
                return games.get(gameId);
        }

        public List<GameState> getGames() {
                return new ArrayList<>(games.values());
        }

}
