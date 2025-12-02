package utils.messages;

import java.io.Serializable;

public class Message implements Serializable {

        private Code code;
        private String info;

        public Message(Code code) {
                this(code, null);
        }

        public Message(Code code, String info) {
                this.code = code;
                this.info = info;
        }

        public Code getCode() {
                return code;
        }

        public String getInfo() {
                return info;
        }

}
