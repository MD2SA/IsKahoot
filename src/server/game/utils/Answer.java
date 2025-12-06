package server.game.utils;

import java.io.Serializable;

public class Answer implements Serializable {

        private final String username;
        private final int option;
        private final int roundNumber;

        public Answer(String username, int option, int roundNumber) {
                this.username = username;
                this.option = option;
                this.roundNumber = roundNumber;
        }

        public String getUsername() {
                return username;
        }

        public int getOption() {
                return option;
        }

        public int getRoundNumber() {
                return roundNumber;
        }
}
