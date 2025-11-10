package server.game;

import server.game.utils.Player;
import server.game.utils.Team;

import java.util.*;

public class TeamManager {

        // Maps for o(1) search
        // userId, playerObj
        private final Map<String, Player> playerMap = new HashMap<>();
        //teamId, teamObj
        private final Map<String, Team> teamMap = new HashMap<>();

        private final int numberOfTeams;
        private final int teamCapacity;

        public TeamManager(int numberOfTeams, int teamCapacity) {
                this.teamCapacity = teamCapacity;
                this.numberOfTeams = numberOfTeams;
        }

        public synchronized boolean registerPlayer(String username, String teamName) {
                if (playerMap.containsKey(username)) // player is already registered
                        return false;

                if( !teamMap.containsKey(teamName) )  {
                        if( numberOfTeams <= teamMap.size() ) // max teams registered
                                return false;
                        teamMap.put(teamName, new Team(teamName,teamCapacity));
                }

                Player player = new Player(username);
                if(!teamMap.get(teamName).addPlayer(player)) // failed to add to team
                        return false;

                playerMap.put(username, player);
                return true;
        }

        public Player getPlayer(String username) {
                synchronized (playerMap) {
                        return playerMap.get(username);
                }
        }

        public Collection<Team> getTeams() {
                synchronized (teamMap) {
                        return teamMap.values();
                }
        }

}
