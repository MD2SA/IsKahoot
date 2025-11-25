package server;

import java.io.Serializable;

import utils.Code;
import utils.quiz.Question;

public class Message implements Serializable {

        private Code code;

        public Message(Code code) {
                this.code = code;
        }

        public Code getCode() {
                return code;
        }

}
