package server;

import utils.Code;

public class MessageFromClient extends Message {

        private int clientId;
        private int gameId;
        private int option;

        public MessageFromClient(Code code, int clientId, int gameId) {
                super(code);
                this.clientId = clientId;
                this.gameId = gameId;
        }

        public int getClientId() {
                return clientId;
        }

        public int getGameId() {
                return gameId;
        }

        public int getAnswer() {
                return option;
        }

}
