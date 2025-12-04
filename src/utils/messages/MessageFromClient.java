package utils.messages;

public class MessageFromClient extends Message {

        private String clientId;
        private String gameId;
        private String teamId;
        private int option;
        private int roundNumber;

        private MessageFromClient(Code code, String clientId, String teamId, String gameId,
                        int option, int roundNumber) {
                super(code);
                this.clientId = clientId;
                this.gameId = gameId;
                this.teamId = teamId;
                this.option = option;
                this.roundNumber = roundNumber;
        }

        public static MessageFromClient getRegistrationMessage(String clientId, String teamId, String gameId) {
                return new MessageFromClient(Code.REGISTER, clientId, teamId, gameId, -1, -1);
        }

        public static MessageFromClient getAnswerQuestion(String clientId, int option, int roundNumber) {
                return new MessageFromClient(Code.ANSWER, clientId, null, null, option, roundNumber);
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

        public int getRoundNumber() {
                return roundNumber;
        }

        public int getAnswer() {
                return option;
        }

        @Override
        public String toString() {
                return """
                                Code: %s
                                GameId: %s
                                ClientId: %s
                                Option: %s
                                """
                                .formatted(getCode().name(), this.gameId, this.clientId, this.option);
        }

}
