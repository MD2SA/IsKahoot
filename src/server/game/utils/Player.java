package server.game.utils;

public class Player {

        private int score;

        public Player() {}

        public Player(int initialScore) {
                this.score = initialScore;
        }

        public int getScore() {
                return score;
        }

        public void setScore(int score) {
                this.score = score;
        }
}
