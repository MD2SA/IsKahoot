package utils.messages;

import java.util.List;

import utils.quiz.Score;
import utils.quiz.ReducedQuestion;

public class MessageFromServer extends Message {

        private int roundNumber = -1;
        private List<Score> scoreboard;
        private ReducedQuestion question;

        private MessageFromServer(Code code, int roundNumber, ReducedQuestion question, int timer,
                        List<Score> scoreboard) {
                super(code);
                this.roundNumber = roundNumber;
                this.question = question;
                this.scoreboard = scoreboard;
        }

        public static MessageFromServer connectionMessage() {
                return new MessageFromServer(Code.REGISTER, -1, null, 10, null);
        }

        public static MessageFromServer updateMessage(int roundNumber, ReducedQuestion question,
                        List<Score> scoreboard) {
                return new MessageFromServer(Code.UPDATE, roundNumber, question, -1, scoreboard);
        }

        public static MessageFromServer endMessage(List<Score> scoreboard) {
                return new MessageFromServer(Code.END, -1, null, -1, scoreboard);
        }

        public int getRoundNumber() {
                return roundNumber;
        }

        public ReducedQuestion getQuestion() {
                return question;
        }

        public List<Score> getScoreboard() {
                return scoreboard;
        }

}
