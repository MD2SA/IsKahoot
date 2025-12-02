package utils.quiz;

import java.util.List;

public class Quiz {

        private String name;
        private int curQuestion = -1;
        private List<Question> questions;

        private Quiz(String name, List<Question> questions) {
                this.name = name;
                this.questions = questions;
        }

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

        // not implemented yet
        public Quiz deepCopy() {
                return this;
        }

}
