package server;

import server.TUI.TUI;
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

public class Server {

        private final static int PORT = 8080;

        private static Server instance;

        private int lastGameId;
        private final List<Quiz> quizzes;

        private final ConcurrentMap<String, GameState> games = new ConcurrentMap<>();
        // gameId, clientId
        private final ConcurrentMap<String, Set<DealWithClient>> gameMap = new ConcurrentMap<>();
        // clientId, gameId
        private final ConcurrentMap<DealWithClient, String> clientMap = new ConcurrentMap<>();

        private Server() throws IOException {
                this.quizzes = QuizCreator.getFrom(new File("quizzes.json"));
        }

        public static Server getInstance() {
                if (instance == null) {
                        try {

                                instance = new Server();
                        } catch (IOException e) {
                                System.out.println("Unable to start server" + e);
                        }
                }
                return instance;
        }

        void createGame() {
                int[] gameInfo = TUI.gatherGameInfo(quizzes);
                String gameId = String.valueOf(lastGameId++);

                int numberOfTeams = gameInfo[0];
                int teamCapacity = gameInfo[1];
                int quizNumber = gameInfo[2];
                Quiz quiz = quizzes.get(quizNumber);

                GameState gameState = new GameState(gameId, teamCapacity, numberOfTeams, quiz);

                games.put(gameId, gameState);
                gameMap.put(gameId, new HashSet<>());
        }

        boolean registerPlayer(DealWithClient handler) {
                String clientId = handler.getClientId();
                String gameId = handler.getGameId();
                GameState game = games.get(gameId);
                if (game == null) {
                        System.out.println("GAME == NULL");
                        return false;
                }

                if (!game.registerPlayer(clientId, handler.getTeamId())) {
                        System.out.println("FAILED ON GAME.REGISTER");
                        return false;
                }

                Set<DealWithClient> set = gameMap.get(gameId);
                if (set == null) {
                        System.out.println("Set is null");
                        return false;
                }
                set.add(handler);

                clientMap.put(handler, gameId);
                return true;
        }

        void answer(DealWithClient handler, int option) {
                String gameId = handler.getGameId();
                GameState game = games.get(gameId);
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
                // TODO: close this properly
                // Set<DealWithClient> set = gameMap.get(handler.getGameId());
                // if (set == null)
                //         return;
                // set.remove(handler);
                //
                // clientMap.remove(handler);
        }

        public void startServing() throws IOException {
                ServerSocket ss = new ServerSocket(PORT);
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
