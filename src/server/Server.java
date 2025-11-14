package server;
import server.TUI.TUI;
import server.game.utils.Score;
import utils.quiz.Question;
import utils.quiz.Quiz;
import utils.quiz.QuizCreator;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Server {

        private static Server instance;
        private int lastGameId;
        //gameId->set<playerId>
        private final Map<Integer, Set<DealWithClient>> games = new HashMap<>();
        private final List<Quiz> quizzes;

        private Server() throws IOException {
                this.quizzes = QuizCreator.getFrom(new File("quizzes.json"));
        }

        public static Server getInstance() throws IOException {
                if( instance == null ) instance = new Server();
                return instance;
        }

        public void createGame() {
                TUI.gatherGameInfo(quizzes);
                games.put(lastGameId++, new HashSet<>());
        }

        public synchronized void sendTeamScores(int roomId, List<Score> scoreboardValues) {
                /* TODO:
                 * - [ ] send scores to all threads of roomId
                 */
        }

        public void serveNextQuestion(int gameId, Question question) {
                if(!games.containsKey(gameId) ) return;
                Set<DealWithClient> playerSet = games.get(gameId);
                playerSet.forEach(player->{
                        System.out.println("Player receiving new question");
                });
        }

        public void connect(int playerId, int gameId, int teamId ) {
                /* TODO:
                 * - [ ] validate playerId
                 * - [ ] validate gameId
                 * - [ ] try to join game
                 * - [ ] send status to client
                 */
        }

        public synchronized void answerFrom(int option, String username){

        }

}