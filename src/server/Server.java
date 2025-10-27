package server;
import java.util.HashMap;
import java.util.Map;

public class Server {

        // gameId, scores
        private final Map<Integer, Map<Integer, Integer>> rooms = new HashMap<>();
        // player->room
        private final Map<Integer, Integer> playersRoom = new HashMap<>();

        public synchronized void sendTeamScores(int roomId, Map<Integer, Integer> scores) {
                notifyAll();
        }

        public void notifyFromGame(int id) {
        }

        public void connect(int playerId, int gameId, int teamId ) {
                /** TODO:
                 * - [ ] validate playerId
                 * - [ ] validate gameId
                 * - [ ] try to join game
                 * - [ ] send status to client
                */
        }

}