package server.game.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Team implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final int capacity;
    private final Set<Player> players = new HashSet<>();

    public Team(String name, int capacity) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Team name must not be blank");
        if (capacity <= 0)
            throw new IllegalArgumentException("Team capacity must be positive");
        this.capacity = capacity;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public synchronized boolean addPlayer(Player player) {
        return capacity > players.size() && players.add(player);
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
