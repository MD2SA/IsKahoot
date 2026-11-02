package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;

import javax.swing.SwingUtilities;

import client.gui.GUI;
import client.gui.Observer.Observed;
import client.gui.Observer.Observer;
import configs.Config;
import utils.messages.MessageFromClient;
import utils.messages.MessageFromServer;
import utils.quiz.ReducedQuestion;

public class Client extends Thread implements Observer {

    private final GUI gui;
    private final String username;
    private final String gameId;
    private final String teamId;

    private volatile int roundNumber = -1;
    private int answeredRound = -1;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public Client(String username, String teamId, String gameId) {
        this.username = username;
        this.gameId = gameId;
        this.teamId = teamId;

        gui = new GUI(username, teamId);
        gui.registerObserver(this);
    }

    private void connectToServer() throws IOException {
        InetAddress addr = InetAddress.getByName(null);
        socket = new Socket(addr, Config.Server.SERVER_PORT);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());

        send(MessageFromClient.getRegistrationMessage(username, teamId, gameId));
    }

    private void communicate() throws IOException, ClassNotFoundException {
        while (true) {
            MessageFromServer message = (MessageFromServer) in.readObject();

            switch (message.getCode()) {
                case QUESTION -> handleQuestion(message);
                case SCOREBOARD -> handleScoreboard(message);
                case END -> {
                    SwingUtilities.invokeLater(gui::displayEndGame);
                    return;
                }
                case ERROR -> {
                    SwingUtilities.invokeLater(() -> gui.displayError(message.getInfo()));
                    return;
                }
                default -> {
                    // Ignore messages that are not part of the game loop.
                }
            }
        }
    }

    private void handleQuestion(MessageFromServer message) {
        ReducedQuestion question = message.getQuestion();
        if (question == null)
            return;

        roundNumber = question.getRoundNumber();
        answeredRound = -1;

        SwingUtilities.invokeLater(() -> {
            gui.startTimer(Config.Features.ROUND_SECONDS);
            gui.displayQuestion(question);
        });
    }

    private void handleScoreboard(MessageFromServer message) {
        Map<String, Integer> scoreboard = message.getScoreboard();
        if (scoreboard == null)
            return;

        SwingUtilities.invokeLater(() -> {
            gui.stopTimer();
            gui.displayScoreboard(scoreboard);
        });
    }

    private void sendAnswer(int option) {
        int round = roundNumber;
        if (round < 0 || answeredRound == round)
            return;

        try {
            send(MessageFromClient.getAnswerQuestion(username, teamId, option, round));
            answeredRound = round;
        } catch (IOException e) {
            gui.displayError("Erro ao enviar a resposta ao servidor.");
        }
    }

    private void send(MessageFromClient message) throws IOException {
        synchronized (out) {
            out.writeObject(message);
            out.flush();
            out.reset();
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
            SwingUtilities.invokeLater(() -> gui.displayError("Erro ao comunicar com o servidor."));
        } finally {
            SwingUtilities.invokeLater(gui::stopTimer);
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null)
                socket.close();
        } catch (Exception e) {
            System.err.println("Failed to close client resources: " + e.getMessage());
        }
    }

}
