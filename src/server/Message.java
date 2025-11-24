package server;

import java.io.Serializable;

import utils.Code;
import utils.quiz.Question;

public class Message implements Serializable {

        private Code code;
        private Question question;

        public Message(Code code) {
                this.code = code;
        }

        public Code getCode() {
                return code;
        }

        public Question getQuestion() {
                return question;
        }

}
