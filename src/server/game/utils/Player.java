package server.game.utils;

public class Player {

        private int score;
        private String name;

        public Player(String name) {
                this.name = name;
        }

        public Player(String name, int initialScore) {
                this.name = name;
                this.score = initialScore;
        }

        public String getName() { return name; }

        public int getScore() {
                return score;
        }

        public void setScore(int score) {
                this.score = score;
        }
}
