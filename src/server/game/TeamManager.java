package server.game;

import server.game.utils.Player;
import server.game.utils.Score;
import server.game.utils.Team;

import java.util.*;

public class TeamManager {

        // Maps for o(1) search
        // userId, playerObj
        private final Map<String, Player> playerMap = new HashMap<>();
        //teamId, teamObj
        private final Map<String, Team> teamMap;

        private final int numberOfTeams;
        private final int teamCapacity;

        public TeamManager(int numberOfTeams, int teamCapacity) {
                // teamcapacity == 1 means singlePlayer mode
                this.teamMap = isMultiPlayer() ? new HashMap<>() : null;
                this.teamCapacity = teamCapacity;
                this.numberOfTeams = numberOfTeams;
        }

        public synchronized boolean registerPlayer(String username, String teamName) {
                if (playerMap.containsKey(username)) // player is already registered
                        return false;

                if( isMultiPlayer() && !teamMap.containsKey(teamName) )  {
                        if( numberOfTeams <= teamMap.size() ) // max teams registered
                                return false;
                        teamMap.put(teamName, new Team(teamName,teamCapacity));
                }

                Player player = new Player(username);
                if( isMultiPlayer() && !teamMap.get(teamName).addPlayer(player)) // failed to add to team
                        return false;

                playerMap.put(username, player);
                return true;
        }

        public Player getPlayer(String username) {
                synchronized (playerMap) {
                        return playerMap.get(username);
                }
        }

        public List<Score> getScoreBoardValues() {
                List<Score> scores = new ArrayList<>();
                if(teamCapacity == 1)
                        synchronized (playerMap) {
                                return new ArrayList<>(playerMap.values());
                        }
                else
                        synchronized (teamMap) {
                                return new ArrayList<>(teamMap.values());
                        }
        }

        public boolean isMultiPlayer() {
                return teamCapacity > 1;
        }

}
