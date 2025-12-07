package server.game;

import server.Server;
import server.game.utils.Player;
import server.game.utils.Score;
import server.game.utils.Team;
import utils.tools.ConcurrentMap;

import java.util.*;

public class TeamManager {

        // Maps for o(1) search
        // userId, playerObj
        private final ConcurrentMap<String, Player> playerMap = new ConcurrentMap<>();
        // teamId, teamObj
        private final ConcurrentMap<String, Team> teamMap;

        private final int numberOfTeams;
        private final int teamCapacity;

        public TeamManager(int numberOfTeams, int teamCapacity) {
                // teamcapacity == 1 means singlePlayer mode
                this.numberOfTeams = numberOfTeams;
                this.teamCapacity = teamCapacity;
                this.teamMap = isMultiPlayer() ? new ConcurrentMap<>() : null;
        }

        public synchronized boolean registerPlayer(String username, String teamName) {
                // player is already registered
                if (playerMap.containsKey(username)) {
                        Server.getInstance().getLogger()
                                        .debug("TeamManager registerPlayer: " + username + " is already registered");
                        return false;
                }

                if (isMultiPlayer() && !teamMap.containsKey(teamName)) {
                        // max teams registered
                        if (numberOfTeams <= teamMap.size()) {
                                Server.getInstance().getLogger().debug("TeamManager registerPlayer: max teams reached");
                                return false;
                        }
                        teamMap.put(teamName, new Team(teamName, teamCapacity));
                }

                Player player = new Player(username);
                // failed to add to team
                if (isMultiPlayer() && !teamMap.get(teamName).addPlayer(player)) {
                        Server.getInstance().getLogger().debug("TeamManager registerPlayer: failed to add to team");
                        return false;
                }

                playerMap.put(username, player);
                return true;
        }

        public Player remove(String username) {
                return playerMap.remove(username);
        }

        public Player getPlayer(String username) {
                return playerMap.get(username);
        }

        public List<Score> getScoreboard() {
                if (teamCapacity == 1)
                        return new ArrayList<>(playerMap.values());
                else
                        return new ArrayList<>(teamMap.values());
        }

        public boolean isMultiPlayer() {
                return teamCapacity > 1;
        }

        public boolean isFull() {
                return teamCapacity * numberOfTeams <= playerMap.size();
        }

        public boolean hasPlayer(String username) {
                return playerMap.containsKey(username);
        }

}
