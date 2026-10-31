package utils.messages;

import utils.quiz.ReducedQuestion;
import utils.quiz.RoundResult;

public class MessageFromServer extends Message {

    private RoundResult result;
    private ReducedQuestion question;

    private MessageFromServer(Code code, ReducedQuestion question, int timer, RoundResult result) {
        super(code);
        this.question = question;
        this.result = result;
    }

    public static MessageFromServer connectionMessage() {
        return new MessageFromServer(Code.REGISTER, null, 10, null);
    }

    public static MessageFromServer questionMessage(ReducedQuestion question) {
        return new MessageFromServer(Code.QUESTION, question, -1, null);
    }

    public static MessageFromServer resultMessage(RoundResult result) {
        return new MessageFromServer(Code.QUESTION, null, -1, null);
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

    public RoundResult getResult() {
        return result;
    }

}
