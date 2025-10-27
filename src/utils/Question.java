package utils;

import java.util.List;

public record Question(String label, int points, int correct, List<String> options) {

        public boolean isCorrect(int option) {
                return option == correct;
        }

}
