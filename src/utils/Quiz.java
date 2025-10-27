package utils;

import java.util.List;

public class Quiz {

        private final String name;
        private int curQuestion;
        private final List<Question> questionList;

        public Quiz(String name, List<Question> questionList) {
                this.name = name;
                this.curQuestion = -1;
                this.questionList = questionList;
        }

        public boolean hasNextQuestion() {
                return questionList.size() > (curQuestion + 1);
        }

        public Question nextQuestion() {
                if(!hasNextQuestion()) return null;
                return questionList.get(++curQuestion);
        }

        public Question getCurQuestion() {
                return questionList.get(curQuestion);
        }

}
