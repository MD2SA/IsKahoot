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

        private static final ExecutorService GAME_EXECUTOR = Executors.newFixedThreadPool(5);

        private final List<Quiz> quizzes;
        private final Scanner scanner = new Scanner(System.in);
        private final CommandDispatcher dispatcher;

        private final ConcurrentMap<String, GameState> games = new ConcurrentMap<>();

        public GameManager() throws IOException {
                this.quizzes = QuizCreator.getFrom(new File("quizzes.json"));
                this.dispatcher = new CommandDispatcher(new CommandContext(this));
        }

        public void addNewGame(GameState gameState) {
                if (gameState == null)
                        return;
                Server.getInstance().getLogger().debug("GameManager adding new game" + gameState.getCode());
                games.put(gameState.getCode(), gameState);
                Server.getInstance().registerGame(gameState.getCode());
        }

        public void removeGame(String gameId) {
                games.remove(gameId);
        }

        public boolean startGameWhenFull(GameState gameState) {
                if (gameState == null || !gameState.isFull())
                        return false;
                GAME_EXECUTOR.submit(gameState);
                return true;
        }

        // public void runGameLoop(GameState gameState) {
        //         new Thread(() -> {
        //                 gameState.start();
        //         }).start();
        // }

        @Override
        public void run() {
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
