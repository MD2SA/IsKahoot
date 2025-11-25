package utils.messages;

public class MessageFromClient extends Message {

        private String clientId;
        private String gameId;
        private String teamId;
        private int option;

        public MessageFromClient(Code code, String clientId, String teamId, String gameId) {
                this(code, clientId, teamId, gameId, -1);
        }

        public MessageFromClient(Code code, String clientId, String teamId, String gameId, int option) {
                super(code);
                this.clientId = clientId;
                this.gameId = gameId;
                this.teamId = teamId;
                this.option = option;
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
