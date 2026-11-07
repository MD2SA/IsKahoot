package client;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import client.gui.GUI;
import client.gui.Observer.Observed;
import client.gui.Observer.Observer;
import configs.Config;
import utils.messages.ClientMessage;
import utils.messages.MessageCodec;
import utils.messages.Protocol;
import utils.messages.ProtocolObjectFilters;
import utils.messages.ServerMessage;
import utils.quiz.ReducedQuestion;

public final class Client implements Runnable, Observer {

    private static final int OUTBOUND_CAPACITY = 16;

    private final GUI gui;
    private final String username;
    private final String gameId;
    private final String teamId;
    private final BlockingQueue<ClientMessage> outbound = new ArrayBlockingQueue<>(OUTBOUND_CAPACITY);
    private final AtomicBoolean resourcesClosed = new AtomicBoolean();

    // These fields are accessed only on the Swing event-dispatch thread.
    private int roundNumber = -1;
    private int answeredRound = -1;

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private Thread writerThread;

    public Client(String username, String teamId, String gameId) {
        this.username = username;
        this.gameId = gameId;
        this.teamId = teamId;

        gui = new GUI(username, teamId);
        gui.registerObserver(this);
    }

    private void connectToServer() throws IOException {
        InetAddress addr = InetAddress.getByName(Config.Server.SERVER_IP);
        socket = new Socket(addr, Config.Server.SERVER_PORT);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());

        startWriter();
        if (!enqueue(new ClientMessage.Register(username, teamId, gameId, Protocol.CURRENT_VERSION)))
            throw new IOException("Unable to queue registration message");
    }

    private void startWriter() {
        writerThread = new Thread(this::writeLoop, "server-message-writer-" + username);
        writerThread.setDaemon(true);
        writerThread.start();
    }

    private void communicate() throws IOException, ClassNotFoundException {
        while (!resourcesClosed.get()) {
            Object value = MessageCodec.read(in, ProtocolObjectFilters.serverToClient());
            if (!(value instanceof ServerMessage message))
                throw new InvalidObjectException("Unexpected server message type");

            switch (message) {
                case ServerMessage.Question question -> handleQuestion(question.question());
                case ServerMessage.Scoreboard scoreboard -> handleScoreboard(scoreboard);
                case ServerMessage.AnswerAccepted accepted -> handleAnswerAccepted(accepted);
                case ServerMessage.AnswerRejected rejected -> handleAnswerRejected(rejected);
                case ServerMessage.End ignored -> {
                    SwingUtilities.invokeLater(gui::displayEndGame);
                    return;
                }
                case ServerMessage.Error error -> {
                    SwingUtilities.invokeLater(() -> gui.displayError(error.reason()));
                    return;
                }
            }
        }
    }

    private void handleQuestion(ReducedQuestion question) {
        SwingUtilities.invokeLater(() -> {
            roundNumber = question.getRoundNumber();
            answeredRound = -1;
            gui.startTimer(question.getDeadlineEpochMillis());
            gui.displayQuestion(question);
        });
    }

    private void handleScoreboard(ServerMessage.Scoreboard scoreboard) {
        SwingUtilities.invokeLater(() -> {
            answeredRound = -1;
            gui.stopTimer();
            gui.displayScoreboard(scoreboard.scores());
        });
    }

    private void handleAnswerAccepted(ServerMessage.AnswerAccepted accepted) {
        SwingUtilities.invokeLater(() -> {
            if (roundNumber == accepted.roundNumber() && answeredRound == accepted.roundNumber())
                gui.displayWaiting();
        });
    }

    private void handleAnswerRejected(ServerMessage.AnswerRejected rejected) {
        SwingUtilities.invokeLater(() -> {
            if (roundNumber == rejected.roundNumber() && answeredRound == rejected.roundNumber()) {
                answeredRound = -1;
                gui.displayError(rejected.reason());
            }
        });
    }

    private void sendAnswer(int option) {
        int round = roundNumber;
        if (round < 0 || answeredRound == round)
            return;

        answeredRound = round;
        gui.displaySubmitting();
        if (!enqueue(new ClientMessage.SubmitAnswer(option, round))) {
            answeredRound = -1;
            gui.displayError("Erro ao enviar a resposta ao servidor.");
        }
    }

    private boolean enqueue(ClientMessage message) {
        if (!resourcesClosed.get() && outbound.offer(message))
            return true;

        closeResources();
        return false;
    }

    private void writeLoop() {
        try {
            while (!resourcesClosed.get()) {
                ClientMessage message = outbound.take();
                MessageCodec.write(out, message);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            if (!resourcesClosed.get())
                SwingUtilities.invokeLater(() -> gui.displayError("Erro ao comunicar com o servidor."));
            closeResources();
        }
    }

    @Override
    public void update(Observed source) {
        if (gui.hasSelectedOption())
            sendAnswer(gui.getSelectedOption());
    }

    @Override
    public void run() {
        try {
            connectToServer();
            communicate();
        } catch (IOException | ClassNotFoundException e) {
            if (!resourcesClosed.get())
                SwingUtilities.invokeLater(() -> gui.displayError("Erro ao comunicar com o servidor."));
        } finally {
            SwingUtilities.invokeLater(gui::stopTimer);
            closeResources();
        }
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private void closeResources() {
        if (!resourcesClosed.compareAndSet(false, true))
            return;

        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            System.err.println("Failed to close client socket: " + e.getMessage());
        }

        Thread writer = writerThread;
        if (writer != null && writer != Thread.currentThread())
            writer.interrupt();
    }
}
