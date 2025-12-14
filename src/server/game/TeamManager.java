package server.game;

import java.util.Map;

import server.game.utils.Player;
import server.game.utils.Team;
import utils.tools.ConcurrentMap;
import utils.tools.ConcurrentSet;

public class TeamManager {

        private final ConcurrentSet<String> usernames = new ConcurrentSet<>();
        private final ConcurrentMap<String, Team> teams = new ConcurrentMap<>();
        private final int numberOfTeams;
        private final int teamCapacity;

        public TeamManager(int numberOfTeams, int teamCapacity) {
                this.numberOfTeams = numberOfTeams;
                this.teamCapacity = teamCapacity;
        }

        public synchronized boolean registerPlayer(String username, String teamName) {
                if (usernames.contains(username))
                        return false;

                if (!teams.containsKey(teamName) && numberOfTeams <= teams.size())
                        return false;
                teams.putIfAbsent(teamName, new Team(teamName, teamCapacity));
                Team team = teams.get(teamName);

                if (!team.addPlayer(new Player(username)))
                        return false;

                usernames.add(username);
                return true;

        }

        public synchronized void removePlayer(String username) {
                usernames.remove(username);
                teams.values().forEach(t -> t.removePlayer(username));
        }

        public boolean isFull() {
                return usernames.size() >= numberOfTeams * teamCapacity;
        }

        public Map<String, Team> getTeams() {
                return teams;
        }

        public ConcurrentSet<String> getUsernames() {
                return usernames;
        }
}
