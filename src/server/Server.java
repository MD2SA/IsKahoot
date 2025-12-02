package server;

import server.game.GameState;
import server.game.utils.Score;
import server.utils.Logger;
import utils.quiz.Question;
import utils.quiz.Quiz;
import utils.quiz.QuizCreator;
import utils.tools.ConcurrentMap;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import configs.Config;

public class Server {

        private static Server instance;

        private final List<Quiz> quizzes;
        private final GameManager gameManager;
        private final Logger logger;

        // gameId, set<connection>
        private final ConcurrentMap<String, Set<DealWithClient>> gameMap = new ConcurrentMap<>();
        // clientId, gameId
        private final ConcurrentMap<DealWithClient, String> clientMap = new ConcurrentMap<>();

        private Server() throws IOException {
                this.quizzes = QuizCreator.getFrom(new File("quizzes.json"));
                this.gameManager = new GameManager(quizzes);
                this.logger = new Logger();
                gameManager.start();
        }

        public Logger getLogger() {
                return logger;
        }

        public static Server getInstance() {
                if (instance == null) {
                        try {
                                instance = new Server();
                        } catch (IOException e) {
                                System.out.println("Unable to start server");
                                e.printStackTrace();
                        }
                }
                return instance;
        }

        public boolean registerGame(String gameId) {
                logger.debug("Trying to register game (step 1/2) " + gameId);
                if (gameMap.containsKey(gameId))
                        return false;
                gameMap.put(gameId, new HashSet<>());
                logger.debug("Successfully registered game (step2/2) " + gameId);
                return true;
        }

        boolean registerPlayer(DealWithClient handler) {
                String clientId = handler.getClientId();
                String gameId = handler.getGameId();
                GameState game = gameManager.getGame(gameId);
                if (game == null) {
                        logger.debug("Server registerPlayer: game " + gameId + "does not exist");
                        return false;
                }
                if (game.hasStarted()) {
                        logger.warn("Server registerPlayer: game has already started");
                        return false;
                }

                if (!game.registerPlayer(clientId, handler.getTeamId())) {
                        logger.debug("Server registerPlayer: game.registerPlayer() returned false");
                        return false;
                }

                Set<DealWithClient> set = gameMap.get(gameId);
                if (set == null) {
                        logger.warn("Server registerPlayer: set should not be null. Game probably has been removed");
                        return false;
                }
                set.add(handler);

                clientMap.put(handler, gameId);

                gameManager.startGameWhenFull(game);

                return true;
        }

        void answer(DealWithClient handler, int option) {
                String gameId = handler.getGameId();
                GameState game = gameManager.getGame(gameId);
                if (game == null)
                        return;
                game.answer(String.valueOf(handler.getClientId()), option);
        }

        public void serveNextQuestion(String gameId, Question question) {
                Set<DealWithClient> set = gameMap.get(gameId);
                set.forEach(h -> h.sendQuestion(question));
        }

        public void serveScoreboard(String gameId, List<Score> scoreboard) {
                Set<DealWithClient> set = gameMap.get(gameId);
                set.forEach(h -> h.sendScoreboard(scoreboard));
        }

        public void killGame(String gameId) {
                Set<DealWithClient> clients = gameMap.get(gameId);
                if (clients == null)
                        return;
                for (DealWithClient client : clients) {
                    client.sendEndGame();
                    client.interrupt();
                }
                gameMap.remove(gameId);
                gameManager.removeGame(gameId);
        }

        void close(DealWithClient handler) {
                // Set<DealWithClient> set = gameMap.get(handler.getGameId());
                // if (set == null)
                // return;
                // set.remove(handler);
                // // if is empty and has finished
                // // if (set.isEmpty()) gameManager.removeGame(handler.getGameId());
                //
                // clientMap.remove(handler);
        }

        public void startServing() throws IOException {
                ServerSocket ss = new ServerSocket(Config.Server.SERVER_PORT);
                try {
                        while (true) {
                                Socket socket = ss.accept();
                                logger.info("Accepted socket");
                                new DealWithClient(this, socket).start();
                        }
                } catch (Exception e) {
                } finally {
                        ss.close();
                }
        }

}
