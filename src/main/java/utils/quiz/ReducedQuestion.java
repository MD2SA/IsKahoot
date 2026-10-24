package utils.quiz;

import java.io.Serializable;
import java.util.List;

public class ReducedQuestion implements Serializable {
        private final String question;
        private final int points;
        private final boolean isIndividual;
        private final List<String> options;
        private final int roundNumber;

        public ReducedQuestion(Question question, int roundNumber) {
                this.question = question.getQuestion();
                this.points = question.getPoints();
                this.isIndividual = question.isIndividual();
                this.options = question.getOptions();
                this.roundNumber = roundNumber;
        }

        public String getQuestion() {
                return question;
        }

        public int getPoints() {
                return points;
        }

        public boolean isIndividual() {
                return isIndividual;
        }

        public List<String> getOptions() {
                return options;
        }

        public int getRoundNumber() {
                return roundNumber;
        }

}
