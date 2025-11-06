package utils;

import java.util.List;

public class Quiz {

        private String name;
        private int curQuestion;
        private List<Question> questions;

        public boolean hasNextQuestion() {
                if(questions == null) {System.out.println("IS null");return false;}
                return questions.size() > (curQuestion + 1);
        }

        public Question nextQuestion() {
                return questions.get(++curQuestion);
        }

        public Question getCurQuestion() {
                return questions.get(curQuestion);
        }

}
