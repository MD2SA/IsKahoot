package utils.messages;

import server.game.utils.Answer;

public class MessageFromClient extends Message {

        private String clientId;
        private String gameId;
        private String teamId;
        private Answer answer;

        private MessageFromClient(Code code, String clientId, String teamId, String gameId,
                        int option, int roundNumber) {
                super(code);
                this.clientId = clientId;
                this.gameId = gameId;
                this.teamId = teamId;
                this.answer = new Answer(clientId, option, roundNumber);
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

        public Answer getAnswer() {
                return answer;
        }

}
