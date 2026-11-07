package server.game;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import server.game.utils.Player;
import server.game.utils.Team;
import utils.tools.ConcurrentMap;
import utils.tools.ConcurrentSet;

public final class TeamManager {

    private final ConcurrentSet<String> usernames = new ConcurrentSet<>();
    private final ConcurrentMap<String, Team> teams = new ConcurrentMap<>();
    private final int numberOfTeams;
    private final int teamCapacity;

    public TeamManager(int numberOfTeams, int teamCapacity) {
        if (numberOfTeams <= 0 || teamCapacity <= 0)
            throw new IllegalArgumentException("Team count and capacity must be positive");
        this.numberOfTeams = numberOfTeams;
        this.teamCapacity = teamCapacity;
    }

    public synchronized boolean registerPlayer(String username, String teamName) {
        if (username == null || username.isBlank() || teamName == null || teamName.isBlank())
            return false;

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

    public synchronized boolean isFull() {
        return usernames.size() >= (long) numberOfTeams * teamCapacity;
    }

    synchronized Map<String, String> getRosterSnapshot() {
        Map<String, String> roster = new HashMap<>();
        for (Team team : teams.values()) {
            for (Player player : team.getPlayers())
                roster.put(player.getName(), team.getName());
        }
        return Map.copyOf(roster);
    }

    public synchronized Set<String> getUsernames() {
        return Set.copyOf(usernames);
    }
}
