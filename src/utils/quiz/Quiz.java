package utils.quiz;

import java.util.List;

public class Quiz {

        private String name;
        private int curQuestion = -1;
        private List<Question> questions;

        public String getName() {
                return name;
        }

        public boolean hasNextQuestion() {
                if(questions == null) return false;
                return questions.size() > (curQuestion + 1);
        }

        public Question nextQuestion() {
                return questions.get(++curQuestion);
        }

        public Question getCurQuestion() {
                return questions.get(curQuestion);
        }

}
