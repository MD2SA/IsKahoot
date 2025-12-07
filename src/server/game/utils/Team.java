package server.game.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Team implements Score, Serializable {

        private final String name;
        private final int capacity;
        private final List<Player> players = new ArrayList<>();

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

        public boolean addPlayer(Player player) {
                if(capacity<=players.size()) return false;
                return players.add(player);
        }


}
