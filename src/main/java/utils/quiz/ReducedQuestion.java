package utils.quiz;

import java.io.Serializable;
import java.util.List;

public class ReducedQuestion implements Serializable {
    private final String question;
    private final int points;
    private final boolean isIndividual;
    private final List<String> options;
    private final int roundNumber;
    private final long deadlineEpochMillis;

    public ReducedQuestion(Question question, int roundNumber, long deadlineEpochMillis) {
        this.question = question.getQuestion();
        this.points = question.getPoints();
        this.isIndividual = question.isIndividual();
        this.options = List.copyOf(question.getOptions());
        this.roundNumber = roundNumber;
        this.deadlineEpochMillis = deadlineEpochMillis;
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

    public long getDeadlineEpochMillis() {
        return deadlineEpochMillis;
    }

}
