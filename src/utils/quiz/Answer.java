package utils.quiz;

import java.io.Serializable;

public class Answer implements Serializable {

        private final String teamId;
        private final String username;
        private final int option;
        private final int roundNumber;
        private transient int bonus;

        public Answer(String teamId, String username, int option, int roundNumber) {
                this.teamId = teamId;
                this.username = username;
                this.option = option;
                this.roundNumber = roundNumber;
        }

        public String getTeamId() {
                return teamId;
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

        public int getBonus() {
                return bonus;
        }

        public void setBonus(int bonus) {
                System.out.println("Count down bonus was" + bonus);
                this.bonus = bonus;
        }
}
