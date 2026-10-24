package utils.quiz;

import java.util.List;

public class Question {
        private String question;
        private int points;
        private int correct;
        private boolean isIndividual;
        private List<String> options;

        public String getQuestion() {
                return question;
        }

        public int getPoints() {
                return points;
        }

        public int getCorrect() {
                return correct;
        }

        public boolean isIndividual() {
                return isIndividual;
        }

        public List<String> getOptions() {
                return options;
        }

        public boolean isCorrect(int option) {
                return option == correct;
        }

        @Override
        public String toString() {
                return "label: " + question + "; correct: " + correct+"-"+options.get(correct);
        }

}
