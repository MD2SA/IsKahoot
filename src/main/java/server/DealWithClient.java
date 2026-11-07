package server;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import server.game.AnswerStatus;
import server.game.GameSession;
import utils.messages.ClientMessage;
import utils.messages.MessageCodec;
import utils.messages.Protocol;
import utils.messages.ProtocolObjectFilters;
import utils.messages.ServerMessage;
import utils.quiz.Answer;
import utils.quiz.ReducedQuestion;
import utils.quiz.Scoreboard;

// This adapter intentionally owns one connection; volatile publishes its state to reader/writer/session threads.
@SuppressWarnings({"PMD.GodClass", "PMD.AvoidUsingVolatile"})
public class DealWithClient implements Runnable, AutoCloseable {

    private static final int OUTBOUND_CAPACITY = 64;
    private static final int REGISTRATION_TIMEOUT_MILLIS = 10_000;
    private static final long TERMINAL_WRITE_TIMEOUT_SECONDS = 2;

    private final Server server;
    private final Socket socket;
    private final BlockingQueue<ServerMessage> outbound = new ArrayBlockingQueue<>(OUTBOUND_CAPACITY);
    private final AtomicBoolean closed = new AtomicBoolean();
    private final CountDownLatch writerStopped = new CountDownLatch(1);

    private volatile DataOutputStream out;
    private volatile DataInputStream in;
    private volatile Thread writerThread;

    private volatile String clientId;
    private volatile String teamId;
    private volatile GameSession game;

    public DealWithClient(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            initializeStreams();
            startWriter();

            if (!registerClient())
                return;

            socket.setSoTimeout(0);
            serve();
        } catch (IOException | ClassNotFoundException e) {
            if (!closed.get())
                server.getLogger().warn("Client connection ended after exception: " + clientId + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void initializeStreams() throws IOException {
        socket.setSoTimeout(REGISTRATION_TIMEOUT_MILLIS);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
    }

    private void startWriter() {
        writerThread = new Thread(this::writeLoop, "client-writer-" + socket.getRemoteSocketAddress());
        writerThread.setDaemon(true);
        writerThread.start();
    }

    private boolean registerClient() throws IOException, ClassNotFoundException {
        ClientMessage message = readMessage();
        if (!(message instanceof ClientMessage.Register registration)) {
            sendTerminalError("Expected registration message");
            return false;
        }

        if (registration.protocolVersion() != Protocol.CURRENT_VERSION) {
            sendTerminalError("Unsupported protocol version");
            return false;
        }

        clientId = registration.clientId();
        teamId = registration.teamId();
        Thread.currentThread().setName("client-" + clientId);

        game = server.getGameManager().registerToGame(this, registration.gameId());
        if (game == null) {
            sendTerminalError("Registration failed");
            return false;
        }

        return true;
    }

    private void serve() throws IOException, ClassNotFoundException {
        while (!closed.get() && !Thread.currentThread().isInterrupted()) {
            ClientMessage message = readMessage();
            switch (message) {
                case ClientMessage.SubmitAnswer answer -> handleAnswer(answer);
                case ClientMessage.Register ignored -> {
                    sendTerminalError("Already registered");
                    return;
                }
            }
        }
    }

    private void handleAnswer(ClientMessage.SubmitAnswer answer) {
        Answer submitted = new Answer(teamId, clientId, answer.option(), answer.roundNumber());
        AnswerStatus status = game.receiveAnswer(clientId, submitted);

        switch (status) {
            case ACCEPTED, ROUND_COMPLETED -> enqueue(new ServerMessage.AnswerAccepted(answer.roundNumber()));
            case REJECTED -> enqueue(
                    new ServerMessage.AnswerRejected(answer.roundNumber(), "Answer was not accepted for this round"));
        }
    }

    private ClientMessage readMessage() throws IOException, ClassNotFoundException {
        Object value = MessageCodec.read(in, ProtocolObjectFilters.clientToServer());
        if (value instanceof ClientMessage message)
            return message;

        throw new InvalidObjectException("Unexpected client message type");
    }

    public void sendQuestion(ReducedQuestion question) {
        enqueue(new ServerMessage.Question(question));
    }

    public void sendScoreboard(Scoreboard scoreboard) {
        enqueue(new ServerMessage.Scoreboard(scoreboard.getScoreboard()));
    }

    public void sendEndGame() {
        enqueue(new ServerMessage.End());
    }

    private void sendTerminalError(String reason) {
        if (enqueue(new ServerMessage.Error(reason)))
            awaitWriterStop();
    }

    private boolean enqueue(ServerMessage message) {
        if (closed.get())
            return false;

        if (outbound.offer(message))
            return true;

        server.getLogger().warn("Outbound queue full for client " + clientId + "; disconnecting");
        cleanup();
        return false;
    }

    private void writeLoop() {
        try {
            while (!closed.get()) {
                ServerMessage message = outbound.take();
                MessageCodec.write(out, message);

                if (message instanceof ServerMessage.End || message instanceof ServerMessage.Error)
                    return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            if (!closed.get())
                server.getLogger().warn("Failed to write to client " + clientId + ": " + e.getMessage());
        } finally {
            writerStopped.countDown();
            cleanup();
        }
    }

    private void awaitWriterStop() {
        try {
            if (!writerStopped.await(TERMINAL_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                cleanup();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            cleanup();
        }
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private void cleanup() {
        if (!closed.compareAndSet(false, true))
            return;

        GameSession currentGame = game;
        if (currentGame != null)
            currentGame.leave(this);

        closeQuietly(socket, in, out);

        Thread writer = writerThread;
        if (writer != null && writer != Thread.currentThread())
            writer.interrupt();
    }

    @SuppressWarnings("PMD.CloseResource")
    private void closeQuietly(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            try {
                if (closeable != null)
                    closeable.close();
            } catch (IOException e) {
                server.getLogger().debug("Error closing client resource: " + e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        cleanup();
    }

    public String getClientId() {
        return clientId;
    }

    public String getTeamId() {
        return teamId;
    }
}
