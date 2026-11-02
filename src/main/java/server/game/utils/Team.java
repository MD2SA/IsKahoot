package server.game.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Team implements Serializable {

    private final String name;
    private final int capacity;
    private final transient Set<Player> players = new HashSet<>();

    public Team(String name, int capacity) {
        this.capacity = capacity;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public synchronized boolean addPlayer(Player player) {
        if (capacity <= players.size())
            return false;
        return players.add(player);
    }

    public synchronized void removePlayer(String username) {
        players.removeIf(p -> Objects.equals(p.getName(), username));
    }

    public synchronized int size() {
        return players.size();
    }

    public synchronized List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

}
