package server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import server.game.GameSession;
import testutils.ServerTestSupport;
import utils.messages.ClientMessage;
import utils.messages.MessageCodec;
import utils.messages.Protocol;
import utils.messages.ProtocolObjectFilters;
import utils.messages.ServerMessage;
import utils.quiz.Quiz;
import utils.quiz.QuizCreator;
import utils.tools.ConcurrentMap;

@SuppressWarnings("PMD.ExcessiveImports") // Integration test exercises the full socket/message stack.
class DealWithClientTest {

    private static InputStream originalIn;
    private static Server server;

    @BeforeAll
    static void initializeServerSingletonWithoutConsoleInput() {
        originalIn = System.in;
        server = ServerTestSupport.initializeWithoutConsole();
    }

    @AfterAll
    static void shutdownServerAndRestoreInput() {
        server.getGameManager().shutdown();
        System.setIn(originalIn);
    }

    @Test
    @Timeout(5)
    void rejectsANonRegistrationFirstMessage() throws Exception {
        Outcome outcome = exchange(new ClientMessage.SubmitAnswer(1, 0));

        ServerMessage.Error error = assertInstanceOf(ServerMessage.Error.class, outcome.message());
        assertEquals("Expected registration message", error.reason());
        assertNull(outcome.clientId());
    }

    @Test
    @Timeout(5)
    void rejectsAnUnsupportedProtocolVersion() throws Exception {
        Outcome outcome = exchange(new ClientMessage.Register("alice", "A", "game-1", Protocol.CURRENT_VERSION + 1));

        ServerMessage.Error error = assertInstanceOf(ServerMessage.Error.class, outcome.message());
        assertEquals("Unsupported protocol version", error.reason());
        assertNull(outcome.clientId());
    }

    @Test
    @Timeout(5)
    void rejectsRegistrationForAnUnknownGame() throws Exception {
        Outcome outcome = exchange(new ClientMessage.Register("alice", "A", "missing", Protocol.CURRENT_VERSION));

        ServerMessage.Error error = assertInstanceOf(ServerMessage.Error.class, outcome.message());
        assertEquals("Registration failed", error.reason());
        assertEquals("alice", outcome.clientId());
        assertEquals("A", outcome.teamId());
    }

    @Test
    @Timeout(10)
    void playsAGameOverTheSocket() throws Exception {
        withRegisteredConnection("socket-game", (input, output, handler) -> {
            assertEquals(0, readQuestion(input, 0).question().getRoundNumber());
            MessageCodec.write(output, new ClientMessage.SubmitAnswer(1, 0));

            assertEquals(1, readQuestion(input, 1).question().getRoundNumber());
            MessageCodec.write(output, new ClientMessage.SubmitAnswer(2, 1));

            assertInstanceOf(ServerMessage.End.class, readEnd(input));
            assertEquals("alice", handler.getClientId());
            assertEquals("A", handler.getTeamId());
        });
    }

    @Test
    @Timeout(10)
    void rejectsASecondRegistrationOnAnOpenConnection() throws Exception {
        withRegisteredConnection("second-register", (input, output, handler) -> {
            readQuestion(input, 0);

            MessageCodec.write(output,
                    new ClientMessage.Register("alice", "A", "second-register", Protocol.CURRENT_VERSION));

            ServerMessage.Error error = assertInstanceOf(ServerMessage.Error.class, readServerMessage(input));
            assertEquals("Already registered", error.reason());
        });
    }

    @Test
    @Timeout(10)
    void rejectsAnAnswerOutsideTheCurrentRound() throws Exception {
        withRegisteredConnection("bad-answer", (input, output, handler) -> {
            readQuestion(input, 0);

            MessageCodec.write(output, new ClientMessage.SubmitAnswer(1, 99));

            ServerMessage.AnswerRejected rejection = assertInstanceOf(ServerMessage.AnswerRejected.class,
                    readServerMessage(input));
            assertEquals(99, rejection.roundNumber());
            assertEquals("Answer was not accepted for this round", rejection.reason());
        });
    }

    private static void withRegisteredConnection(String gameId, ClientScenario scenario)
            throws IOException, ClassNotFoundException, InterruptedException, ReflectiveOperationException {
        GameSession session = new GameSession(gameId, loadQuiz(), 1, 1);
        waitingGames(server.getGameManager()).put(gameId, session);

        try (ServerSocket listener = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
                Socket clientSide = new Socket(InetAddress.getLoopbackAddress(), listener.getLocalPort());
                Socket serverSide = listener.accept();
                DealWithClient handler = new DealWithClient(server, serverSide)) {
            clientSide.setSoTimeout(2_000);
            Thread connection = new Thread(handler, "deal-with-client");
            connection.start();

            try (DataOutputStream output = new DataOutputStream(clientSide.getOutputStream());
                    DataInputStream input = new DataInputStream(clientSide.getInputStream())) {
                MessageCodec.write(output, new ClientMessage.Register("alice", "A", gameId, Protocol.CURRENT_VERSION));
                scenario.run(input, output, handler);
            }

            clientSide.close();
            connection.join(2_000);

            assertFalse(connection.isAlive());
        }
    }

    @FunctionalInterface
    private interface ClientScenario {
        void run(DataInputStream input, DataOutputStream output, DealWithClient handler)
                throws IOException, ClassNotFoundException, InterruptedException;
    }

    @Test
    @Timeout(5)
    void logsAnAbruptDisconnect() throws Exception {
        try (ServerSocket listener = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
                Socket clientSide = new Socket(InetAddress.getLoopbackAddress(), listener.getLocalPort());
                Socket serverSide = listener.accept();
                DealWithClient handler = new DealWithClient(server, serverSide)) {
            Thread connection = new Thread(handler, "deal-with-client");
            connection.start();

            clientSide.close();
            connection.join(2_000);

            assertFalse(connection.isAlive());
            assertTrue(server.getLogger().getErrors().stream()
                    .anyMatch(log -> log.message.contains("Client connection ended after exception")));
        }
    }

    @Test
    void ignoresMessagesSentAfterClose() {
        try (DealWithClient handler = new DealWithClient(server, null)) {
            handler.close();

            assertDoesNotThrow(handler::sendEndGame);
        }
    }

    private static Outcome exchange(Serializable request)
            throws IOException, ClassNotFoundException, InterruptedException {
        try (ServerSocket listener = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
                Socket clientSide = new Socket(InetAddress.getLoopbackAddress(), listener.getLocalPort());
                Socket serverSide = listener.accept();
                DealWithClient handler = new DealWithClient(server, serverSide)) {
            Thread connection = new Thread(handler, "deal-with-client");
            connection.start();

            try (DataOutputStream output = new DataOutputStream(clientSide.getOutputStream());
                    DataInputStream input = new DataInputStream(clientSide.getInputStream())) {
                MessageCodec.write(output, request);

                ServerMessage message = readServerMessage(input);
                connection.join(2_000);

                assertFalse(connection.isAlive(), "connection thread should stop after a terminal error");
                return new Outcome(message, handler.getClientId(), handler.getTeamId());
            }
        }
    }

    private static ServerMessage.Question readQuestion(DataInputStream input, int roundNumber)
            throws IOException, ClassNotFoundException {
        while (true) {
            ServerMessage message = readServerMessage(input);
            if (message instanceof ServerMessage.Question question) {
                assertEquals(roundNumber, question.question().getRoundNumber());
                return question;
            }
            if (!(message instanceof ServerMessage.AnswerAccepted) && !(message instanceof ServerMessage.Scoreboard))
                fail("Unexpected message before question: " + message);
        }
    }

    private static ServerMessage.End readEnd(DataInputStream input) throws IOException, ClassNotFoundException {
        while (true) {
            ServerMessage message = readServerMessage(input);
            if (message instanceof ServerMessage.End end)
                return end;
            if (!(message instanceof ServerMessage.AnswerAccepted) && !(message instanceof ServerMessage.Scoreboard))
                fail("Unexpected message before end: " + message);
        }
    }

    private static ServerMessage readServerMessage(DataInputStream input) throws IOException, ClassNotFoundException {
        Object message = MessageCodec.read(input, ProtocolObjectFilters.serverToClient());
        return assertInstanceOf(ServerMessage.class, message);
    }

    @SuppressWarnings({"unchecked", "PMD.AvoidAccessibilityAlteration"}) // Seeds the private registry.
    private static ConcurrentMap<String, GameSession> waitingGames(GameManager manager)
            throws ReflectiveOperationException {
        Field field = GameManager.class.getDeclaredField("waitingGames");
        field.setAccessible(true);
        return (ConcurrentMap<String, GameSession>) field.get(manager);
    }

    private static Quiz loadQuiz() throws IOException {
        try (InputStream input = DealWithClientTest.class.getClassLoader().getResourceAsStream("quiz-fast.json")) {
            return QuizCreator.getFrom(input).get(0);
        }
    }

    private record Outcome(ServerMessage message, String clientId, String teamId) {
    }
}
