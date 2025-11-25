package server.game;

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
                        System.out.println(username);
                        System.out.println("PLAYER IS ALREADY REGISTED");
                        return false;
                }

                if (isMultiPlayer() && !teamMap.containsKey(teamName)) {
                        // max teams registered
                        if (numberOfTeams <= teamMap.size()) {
                                System.out.println("MAX TEAMS REACHED");
                                return false;
                        }
                        teamMap.put(teamName, new Team(teamName, teamCapacity));
                }

                Player player = new Player(username);
                // failed to add to team
                if (isMultiPlayer() && !teamMap.get(teamName).addPlayer(player)) {
                        System.out.println("FAILED TO ADD TO TEAM");
                        return false;
                }

                playerMap.put(username, player);
                return true;
        }

        public Player getPlayer(String username) {
                synchronized (playerMap) {
                        return playerMap.get(username);
                }
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
                return teamCapacity*numberOfTeams <= playerMap.size();
        }

}
