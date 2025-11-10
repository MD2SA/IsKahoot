package server;
import server.game.GameState;
import server.game.utils.Team;
import utils.Question;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Server {

        //gameId->set<playerId>
        private final Map<Integer, Set<DealWithClient>> games = new HashMap<>();

        public synchronized void sendTeamScores(int roomId, Collection<Team> teams) {
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