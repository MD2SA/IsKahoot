package server;

import server.TUI.TUI;
import server.game.GameState;
import server.game.utils.Score;
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

        private final ConcurrentMap<Integer, GameState> games = new ConcurrentMap<>();
        // gameId, clientId
        private final ConcurrentMap<Integer, Set<DealWithClient>> gameMap = new ConcurrentMap<>();
        // clientId, gameId
        private final ConcurrentMap<DealWithClient, Integer> clientMap = new ConcurrentMap<>();

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
                int gameId = lastGameId++;

                int numberOfTeams = gameInfo[0];
                int teamCapacity = gameInfo[1];
                int quizNumber = gameInfo[2];
                Quiz quiz = quizzes.get(quizNumber);

                GameState gameState = new GameState(gameId, teamCapacity, numberOfTeams, quiz);

                games.put(gameId, gameState);
                gameMap.put(gameId, new HashSet<>());
        }

        boolean registerPlayer(DealWithClient handler) {
                int clientId = handler.getClientId();
                int gameId = handler.getGameId();
                GameState game = games.get(gameId);
                if (game == null)
                        return false;

                if (!game.registerPlayer(String.valueOf(clientId), "teste"))
                        return false;

                Set<DealWithClient> set = gameMap.get(gameId);
                if (set == null)
                        return false;
                set.add(handler);

                clientMap.put(handler, gameId);
                return true;
        }

        void answer(DealWithClient handler, int option) {
                int gameId = handler.getGameId();
                GameState game = games.get(gameId);
                if (game == null)
                        return;
                game.answer(String.valueOf(handler.getClientId()), option);

        }

        void serveScoreboard(int gameId, List<Score> scoreboard) {
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
                                new DealWithClient(this, socket).start();
                        }
                } catch (Exception e) {
                } finally {
                        ss.close();
                }
        }

}
