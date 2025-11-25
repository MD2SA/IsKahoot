package server.game.utils;

import java.io.Serializable;

public class Player implements Score, Serializable {

        private int score;
        private String name;

        public Player(String name) {
                this(name, 0);
        }

        public Player(String name, int initialScore) {
                this.name = name;
                this.score = initialScore;
        }

        @Override
        public String getName() { return name; }

        @Override
        public int getScore() {
                return score;
        }

        public void setScore(int score) {
                this.score = score;
        }
}
