package utils.messages;

import java.io.Serializable;

public class Message implements Serializable {

        private Code code;

        public Message(Code code) {
                this.code = code;
        }

        public Code getCode() {
                return code;
        }

}
