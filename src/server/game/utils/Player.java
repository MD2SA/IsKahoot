package server.game.utils;

import java.io.Serializable;

import utils.quiz.Score;

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
        public String getName() {
                return name;
        }

        @Override
        public int getScore() {
                return score;
        }

        public void setScore(int score) {
                this.score = score;
        }

        public void increaseScore(int score) {
                System.out.println("old score" + this.score);
                this.score += score;
                System.out.println("new score" + this.score);
        }

        @Override
        public String toString() {
                return "score: " + score + "- name: " + name;
        }
}
