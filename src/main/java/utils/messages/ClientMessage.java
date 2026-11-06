package utils.messages;

import java.io.Serializable;

public sealed interface ClientMessage extends Serializable permits ClientMessage.Register, ClientMessage.SubmitAnswer {

    record Register(String clientId, String teamId, String gameId, int protocolVersion) implements ClientMessage {
    }

    record SubmitAnswer(int option, int roundNumber) implements ClientMessage {
    }
}
