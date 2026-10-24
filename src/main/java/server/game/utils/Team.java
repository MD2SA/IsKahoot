package server.game.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import utils.quiz.Score;

public class Team implements Score, Serializable {

        private final String name;
        private final int capacity;
        private final transient Set<Player> players = new HashSet<>();
        private int teamScore;

        public Team(String name, int capacity) {
                this.capacity = capacity;
                this.name = name;
        }

        @Override
        public String getName() {
                return name;
        }

        @Override
        public int getScore() {
                return teamScore;
        }

        public synchronized void increaseScore(int score) {
                teamScore += score;
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
