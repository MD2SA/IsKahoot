package utils.messages;

import java.util.List;

import server.game.utils.Score;
import utils.quiz.Question;

public class MessageFromServer extends Message {

        private int roundNumber = -1;
        private List<Score> scoreboard;
        private Question question;

        private MessageFromServer(Code code, int roundNumber, Question question, List<Score> scoreboard) {
                super(code);
                this.roundNumber = roundNumber;
                this.question = question;
                this.scoreboard = scoreboard;
        }

        public static MessageFromServer updateMessage(int roundNumber, Question question, List<Score> scoreboard) {
                return new MessageFromServer(Code.UPDATE, roundNumber, question, scoreboard);
        }

        public static MessageFromServer endMessage(List<Score> scoreboard) {
                return new MessageFromServer(Code.END, -1, null, scoreboard);
        }

        public int getRoundNumber() {
                return roundNumber;
        }

        public Question getQuestion() {
                return question;
        }

        public List<Score> getScoreboard() {
                return scoreboard;
        }

}
