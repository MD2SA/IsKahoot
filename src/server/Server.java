package server;

import server.game.GameState;
import server.game.utils.Score;
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

        private final ConcurrentMap<String, Set<DealWithClient>> gameMap = new ConcurrentMap<>();
        // clientId, gameId
        private final ConcurrentMap<DealWithClient, String> clientMap = new ConcurrentMap<>();

        private Server() throws IOException {
                this.quizzes = QuizCreator.getFrom(new File("quizzes.json"));
                this.gameManager = new GameManager(quizzes);
                gameManager.start();
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
                if (gameMap.containsKey(gameId))
                        return false;
                gameMap.put(gameId, new HashSet<>());
                return true;
        }

        boolean registerPlayer(DealWithClient handler) {
                String clientId = handler.getClientId();
                String gameId = handler.getGameId();
                GameState game = gameManager.getGame(gameId);
                if (game == null) {
                        System.out.println("GAME DOES NOT EXIST");
                        return false;
                }

                if (!game.registerPlayer(clientId, handler.getTeamId())) {
                        System.out.println("FAILED ON GAME.REGISTER");
                        return false;
                }

                Set<DealWithClient> set = gameMap.get(gameId);
                if (set == null) {
                        System.out.println("ILLEGAL STATE. GAME IS NOT REGISTERED IN THE SERVER");
                        return false;
                }
                set.add(handler);

                clientMap.put(handler, gameId);
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

        void close(DealWithClient handler) {
                // Set<DealWithClient> set = gameMap.get(handler.getGameId());
                // if (set == null)
                //         return;
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
                                System.out.println("ACCEPTED SOCKET");
                                new DealWithClient(this, socket).start();
                        }
                } catch (Exception e) {
                } finally {
                        ss.close();
                }
        }

}
