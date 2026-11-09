package utils.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.StreamCorruptedException;
import java.util.List;

import org.junit.jupiter.api.Test;

import utils.quiz.Quiz;
import utils.quiz.QuizCreator;
import utils.quiz.ReducedQuestion;
import utils.quiz.Score;

class ProtocolSerializationTest {

    @Test
    void roundTripsRegistrationRequestThroughClientFilter() throws Exception {
        ClientMessage.Register registration = roundTrip(
                new ClientMessage.Register("alice", "A", "ABCD", Protocol.CURRENT_VERSION),
                ProtocolObjectFilters.clientToServer());

        assertEquals("alice", registration.clientId());
        assertEquals("A", registration.teamId());
        assertEquals("ABCD", registration.gameId());
        assertEquals(Protocol.CURRENT_VERSION, registration.protocolVersion());
    }

    @Test
    void roundTripsAnswerRequestWithoutResendingConnectionIdentity() throws Exception {
        ClientMessage.SubmitAnswer answer = roundTrip(new ClientMessage.SubmitAnswer(2, 3),
                ProtocolObjectFilters.clientToServer());

        assertEquals(2, answer.option());
        assertEquals(3, answer.roundNumber());
    }

    @Test
    void roundTripsQuestionAndDeadlineThroughServerFilter() throws Exception {
        ReducedQuestion question = new ReducedQuestion(loadQuiz().getQuestion(0), 0, 123_456L);
        ServerMessage.Question message = roundTrip(new ServerMessage.Question(question),
                ProtocolObjectFilters.serverToClient());

        assertEquals(123_456L, message.question().getDeadlineEpochMillis());
    }

    @Test
    void roundTripsScoreboardAndTypedErrorThroughServerFilter() throws Exception {
        ServerMessage.Scoreboard scoreboard = roundTrip(new ServerMessage.Scoreboard(List.of(new Score("A", 10))),
                ProtocolObjectFilters.serverToClient());
        ServerMessage error = roundTrip(new ServerMessage.Error("Registration failed"),
                ProtocolObjectFilters.serverToClient());

        assertEquals(List.of(new Score("A", 10)), scoreboard.scores());
        assertInstanceOf(ServerMessage.Error.class, error);
    }

    @Test
    void roundTripsAnswerAcceptedAnswerRejectedAndEndThroughServerFilter() throws Exception {
        ServerMessage.AnswerAccepted accepted = roundTrip(new ServerMessage.AnswerAccepted(2),
                ProtocolObjectFilters.serverToClient());
        ServerMessage.AnswerRejected rejected = roundTrip(new ServerMessage.AnswerRejected(3, "too late"),
                ProtocolObjectFilters.serverToClient());
        ServerMessage end = roundTrip(new ServerMessage.End(), ProtocolObjectFilters.serverToClient());

        assertEquals(2, accepted.roundNumber());
        assertEquals(3, rejected.roundNumber());
        assertEquals("too late", rejected.reason());
        assertInstanceOf(ServerMessage.End.class, end);
    }

    @Test
    void serverMessagesRejectMissingPayloadsAndProtectCollections() {
        assertThrows(NullPointerException.class, () -> new ServerMessage.Question(null));
        assertThrows(NullPointerException.class, () -> new ServerMessage.AnswerRejected(0, null));
        assertThrows(NullPointerException.class, () -> new ServerMessage.Error(null));

        ServerMessage.Scoreboard scoreboard = new ServerMessage.Scoreboard(List.of(new Score("A", 1)));

        assertThrows(UnsupportedOperationException.class, () -> scoreboard.scores().add(new Score("B", 2)));
    }

    @Test
    void inputFilterRejectsUnexpectedSerializedClasses() throws Exception {
        assertThrows(InvalidClassException.class,
                () -> roundTrip(new java.util.Date(), ProtocolObjectFilters.clientToServer()));
        assertThrows(InvalidClassException.class,
                () -> roundTrip(new ClientMessage.Register("alice", "A", "ABCD", Protocol.CURRENT_VERSION),
                        ProtocolObjectFilters.serverToClient()));
    }

    @Test
    void framesHaveIndependentPerMessageDeserializationLimits() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            MessageCodec.write(output, new ClientMessage.SubmitAnswer(1, 0));
            MessageCodec.write(output, new ClientMessage.SubmitAnswer(2, 1));
        }

        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            assertEquals(new ClientMessage.SubmitAnswer(1, 0),
                    MessageCodec.read(input, ProtocolObjectFilters.clientToServer()));
            assertEquals(new ClientMessage.SubmitAnswer(2, 1),
                    MessageCodec.read(input, ProtocolObjectFilters.clientToServer()));
        }
    }

    @Test
    void rejectsFramesAboveTheConfiguredMaximumBeforeAllocatingPayload() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeInt(MessageCodec.MAX_FRAME_BYTES + 1);
        }

        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            assertThrows(StreamCorruptedException.class,
                    () -> MessageCodec.read(input, ProtocolObjectFilters.clientToServer()));
        }
    }

    private static Quiz loadQuiz() throws IOException {
        try (InputStream input = ProtocolSerializationTest.class.getClassLoader()
                .getResourceAsStream("quiz-fast.json")) {
            return QuizCreator.getFrom(input).get(0);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T roundTrip(T value, ObjectInputFilter filter) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            MessageCodec.write(output, (java.io.Serializable) value);
        }
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (T) MessageCodec.read(input, filter);
        }
    }
}
