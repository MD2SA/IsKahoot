package utils.messages;

import java.util.List;

import server.game.utils.Score;
import utils.quiz.Question;

public class MessageFromServer extends Message {

        private List<Score> scoreboard;
        private Question question;

        public MessageFromServer(List<Score> scoreboard) {
                this(scoreboard, null);
        }

        public MessageFromServer(Question question) {
                this(null, question);
        }

        public MessageFromServer(List<Score> scoreboard, Question question) {
                super(Code.POST_ANSWER);
                this.scoreboard = scoreboard;
                this.question = question;
        }

        public Question getQuestion() {
                return question;
        }

        public List<Score> getScoreboard() {
                return scoreboard;
        }

}
