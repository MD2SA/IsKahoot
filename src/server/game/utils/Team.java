package server.game.utils;

import java.util.ArrayList;
import java.util.List;

public class Team implements Score {

        public static final int DEFAULT_CAPACITY = 5;

        private final String name;
        private final int capacity;
        private final List<Player> players = new ArrayList<>();

        public Team(String name) {
                this(name, DEFAULT_CAPACITY);
        }
        public Team(String name, int capacity) {
                this.capacity = capacity;
                this.name = name;
        }

        @Override
        public String getName() { return name; }

        @Override
        public int getScore() {
                return players.stream().mapToInt(Player::getScore).sum();
        }

        public int getCapacity() { return capacity; }

        public List<Player> getPlayers() { return players; }

        public boolean addPlayer(Player player) {
                if(capacity<=players.size()) return false;
                return players.add(player);
        }


}
