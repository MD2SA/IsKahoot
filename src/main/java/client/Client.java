package client;

import client.gui.GUI;
import client.gui.Observer.Observer;
import client.gui.Observer.Observed;

import configs.Config;
import utils.messages.MessageFromClient;
import utils.messages.MessageFromServer;
import utils.quiz.Score;
import utils.quiz.ReducedQuestion;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

public class Client extends Thread implements Observer {

        private final GUI gui;
        private final String username;
        private final String gameId;
        private final String teamId;

        private int roundNumber = -1;

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
                MessageFromClient msgSent = MessageFromClient.getRegistrationMessage(username, teamId, gameId);
                out.writeObject(msgSent);
                out.reset();
        }

        private void communicate() {
                while (true) {
                        MessageFromClient msgSent = MessageFromClient.getRegistrationMessage(username, teamId, gameId);
                        try {
                                out.writeObject(msgSent);
                                out.reset();
                                MessageFromServer msgReceived = (MessageFromServer) in.readObject();

                                this.roundNumber = msgReceived.getRoundNumber();

                                ReducedQuestion question = msgReceived.getQuestion();
                                List<Score> scoreboard = msgReceived.getScoreboard();
                                this.gui.displayScoreboard(scoreboard);
                                if (question == null) {
                                        this.gui.displayEndGame();
                                        return;
                                }
                                this.gui.startTimer(Config.Features.ROUND_SECONDS);
                                this.gui.displayQuestion(question);
                        } catch (Exception e) {
                                gui.displayError("Erro ao comunicar com o servidor.");
                                break;
                        }
                }
        }

        private void sendAnswer(int option) {
                MessageFromClient msgSent = MessageFromClient.getAnswerQuestion(username, teamId, option, roundNumber);
                try {
                        out.writeObject(msgSent);
                        out.reset();
                } catch (Exception e) {
                        gui.displayError("Erro ao enviar a resposta ao servidor.");
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
                } catch (IOException e) {
                        gui.displayError("Não foi possível ligar ao servidor.");
                } finally {
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
