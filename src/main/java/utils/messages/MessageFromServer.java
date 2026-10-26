package utils.messages;

import java.util.List;

import utils.quiz.Score;
import utils.quiz.ReducedQuestion;

public class MessageFromServer extends Message {

    private List<Score> scoreboard;
    private ReducedQuestion question;

    private MessageFromServer(Code code, ReducedQuestion question, int timer, List<Score> scoreboard) {
        super(code);
        this.question = question;
        this.scoreboard = scoreboard;
    }

    public static MessageFromServer connectionMessage() {
        return new MessageFromServer(Code.REGISTER, null, 10, null);
    }

    public static MessageFromServer updateMessage(ReducedQuestion question, List<Score> scoreboard) {
        return new MessageFromServer(Code.UPDATE, question, -1, scoreboard);
    }

    public static MessageFromServer endMessage(List<Score> scoreboard) {
        return new MessageFromServer(Code.END, null, -1, scoreboard);
    }

    public int getRoundNumber() {
        return question != null ? question.getRoundNumber() : -1;
    }

    public ReducedQuestion getQuestion() {
        return question;
    }

    public List<Score> getScoreboard() {
        return scoreboard;
    }

}
