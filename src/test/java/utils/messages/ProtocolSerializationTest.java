package utils.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

import utils.quiz.RoundResult;

class ProtocolSerializationTest {

    @Test
    void roundTripsTheRegistrationMessage() throws Exception {
        MessageFromClient message = roundTrip(MessageFromClient.getRegistrationMessage("alice", "A", "ABCD"));

        assertEquals(Code.REGISTER, message.getCode());
        assertEquals("alice", message.getClientId());
        assertEquals("A", message.getTeamId());
        assertEquals("ABCD", message.getGameId());
        assertEquals(-1, message.getAnswer().getRoundNumber());
    }

    @Test
    void roundTripsTheAnswerMessage() throws Exception {
        MessageFromClient message = roundTrip(MessageFromClient.getAnswerQuestion("alice", "A", 2, 3));

        assertEquals(Code.ANSWER, message.getCode());
        assertEquals("alice", message.getAnswer().getUsername());
        assertEquals("A", message.getAnswer().getTeamId());
        assertEquals(2, message.getAnswer().getOption());
        assertEquals(3, message.getAnswer().getRoundNumber());
    }

    @Test
    void roundTripsTheResultMessage() throws Exception {
        MessageFromServer message = roundTrip(MessageFromServer.resultMessage(new RoundResult()));

        assertEquals(Code.RESULTS, message.getCode());
        assertNotNull(message.getResult());
    }

    @SuppressWarnings("unchecked")
    private static <T> T roundTrip(T value) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(value);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (T) in.readObject();
        }
    }

}
