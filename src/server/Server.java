package server;
import server.TUI.TUI;
import server.game.GameState;
import server.game.utils.Score;
import server.game.utils.Team;
import utils.Question;
import utils.Quiz;
import utils.QuizCreator;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Server {

        private int lastGameId;
        //gameId->set<playerId>
        private final Map<Integer, Set<DealWithClient>> games = new HashMap<>();
        private final List<Quiz> quizzes;

        public Server() throws IOException {
                this.quizzes = QuizCreator.getFrom(new File("quizzes.json"));
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