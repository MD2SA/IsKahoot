package utils.messages;

import java.util.Map;

import utils.quiz.ReducedQuestion;
import utils.quiz.Scoreboard;

public class MessageFromServer extends Message {

    private Scoreboard scoreboard;
    private ReducedQuestion question;

    private MessageFromServer(Code code, ReducedQuestion question, int timer, Scoreboard scoreboard) {
        super(code);
        this.question = question;
        this.scoreboard = scoreboard;
    }

    public static MessageFromServer connectionMessage() {
        return new MessageFromServer(Code.REGISTER, null, 10, null);
    }

    public static MessageFromServer questionMessage(ReducedQuestion question) {
        return new MessageFromServer(Code.QUESTION, question, -1, null);
    }

    public static MessageFromServer scoreboardMessage(Scoreboard scoreboard) {
        return new MessageFromServer(Code.SCOREBOARD, null, -1, scoreboard);
    }

    public static MessageFromServer endMessage() {
        return new MessageFromServer(Code.END, null, -1, null);
    }

    public int getRoundNumber() {
        return question != null ? question.getRoundNumber() : -1;
    }

    public ReducedQuestion getQuestion() {
        return question;
    }

    public Map<String, Integer> getScoreboard() {
        return scoreboard.getScoreboard();
    }

}
