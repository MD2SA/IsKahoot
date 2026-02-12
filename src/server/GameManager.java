package server;

import server.cli.CommandDispatcher;
import server.cli.CommandContext;
import server.game.GameSession;
import utils.quiz.Quiz;
import utils.quiz.QuizCreator;
import utils.tools.ConcurrentMap;
import utils.tools.ThreadPool;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import configs.Config;

public class GameManager extends Thread {

        private final List<Quiz> quizzes;
        private final ConcurrentMap<String, GameSession> waitingGames = new ConcurrentMap<>();
        private final ConcurrentMap<String, GameSession> activeGames = new ConcurrentMap<>();

        private final ThreadPool pool = new ThreadPool(Config.Server.MAX_GAMES);
        private final CommandDispatcher dispatcher;

        public GameManager() throws IOException {
                this.quizzes = QuizCreator.getFrom(new File("quizzes.json"));
                this.dispatcher = new CommandDispatcher(new CommandContext(this));
        }

        public void createGame(String gameId, int teams, int capacity, int quizNumber) {
                Quiz quiz = quizzes.get(quizNumber);
                GameSession game = new GameSession(gameId, quiz, teams, capacity);
                waitingGames.put(gameId, game);
                Server.getInstance().getLogger().debug("Game created: " + gameId);
        }

        public GameSession registerToGame(DealWithClient handler, String gameId) {
                GameSession game = waitingGames.get(gameId);
                if (game == null || !game.join(handler))
                        return null;

                if (game.isFull())
                        startGame(game);
                return game;
        }

        private void startGame(GameSession game) {
                pool.submit(() -> {
                        try {
                                activeGames.put(game.getId(), game);
                                waitingGames.remove(game.getId());
                                game.run();
                        } finally {
                                activeGames.remove(game.getId());
                        }
                });
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

        public boolean hasGame(String gameId) {
                return waitingGames.get(gameId) != null || activeGames.get(gameId) != null;
        }

        public List<GameSession> getWaitingGames() {
                return new ArrayList<>(waitingGames.values());
        }

        public List<GameSession> getActiveGames() {
                return new ArrayList<>(activeGames.values());
        }

        public List<GameSession> getAllGames() {
                List<GameSession> allGames = new ArrayList<>(waitingGames.values());
                allGames.addAll(activeGames.values());
                return allGames;
        }

        void shutdown() {
                List<Runnable> games = pool.shutdownNow();
        }

}
