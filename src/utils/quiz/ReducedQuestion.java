package utils.quiz;

import java.io.Serializable;
import java.util.List;

public class ReducedQuestion implements Serializable {
        private String question;
        private int points;
        private boolean isIndividual;
        private List<String> options;

        public ReducedQuestion(Question question) {
                this.question = question.getQuestion();
                this.points = question.getPoints();
                this.isIndividual = question.isIndividual();
                this.options = question.getOptions();
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

}
