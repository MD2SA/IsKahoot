package server;

import utils.Code;

public class MessageFromClient extends Message {

        private String clientId;
        private String gameId;
        private String teamId;
        private int option;

        public MessageFromClient(Code code, String clientId, String teamId, String gameId) {
                super(code);
                this.clientId = clientId;
                this.gameId = gameId;
                this.teamId = teamId;
        }

        public String getClientId() {
                return clientId;
        }

        public String getGameId() {
                return gameId;
        }

        public String getTeamId() {
                return teamId;
        }

        public int getAnswer() {
                return option;
        }



}
