package utils.messages;

import java.util.List;

import server.game.utils.Score;
import utils.quiz.Question;

public class MessageFromServer extends Message {

        private List<Score> scoreboard;
        private Question question;

        public MessageFromServer() {
                this(null, null);
        }

        public MessageFromServer(List<Score> scoreboard) {
                this(null, scoreboard);
        }

        public MessageFromServer(Question question) {
                this(question, null);
        }

        public MessageFromServer(Question question, List<Score> scoreboard) {
                super(Code.POST_ANSWER);
                this.question = question;
                this.scoreboard = scoreboard;
        }

        public Question getQuestion() {
                return question;
        }

        public List<Score> getScoreboard() {
                return scoreboard;
        }

}
