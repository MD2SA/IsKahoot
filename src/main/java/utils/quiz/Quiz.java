package utils.quiz;

import java.util.List;

public class Quiz {

    private String name;
    private List<Question> questions;

    private Quiz(String name, List<Question> questions) {
        this.name = name;
        this.questions = questions;
    }

    public String getName() {
        return name;
    }

    public Question getQuestion(int idx) {
        return questions.get(idx);
    }

    public int getNumberOfQuestions() {
        return questions.size();
    }

}
