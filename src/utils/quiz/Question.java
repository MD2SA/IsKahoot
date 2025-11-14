package utils.quiz;

import java.util.List;

public class Question {
        private String question;
        private int points;
        private int correct;
        private List<String> options;

        public Question(String label, int points, int correct, List<String> options) {
                this.question = label;
                this.points = points;
                this.correct = correct;
                this.options = options;
        }

        public String getQuestion() { return question; }

        public int getPoints() { return points; }

        public int getCorrect() { return correct; }

        public List<String> getOptions() { return options; }

        public boolean isCorrect(int option) { return option == correct; }

}
