package client;

import client.gui.GUI;
import client.gui.Observer.Observer;
import client.gui.Observer.Observed;

import configs.Config;
import utils.messages.MessageFromClient;
import utils.messages.MessageFromServer;
import server.game.utils.Score;
import utils.messages.Code;
import utils.quiz.Question;

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

        private void comunicate() {
                while (true) {
                        MessageFromClient msgSent = MessageFromClient.getRegistrationMessage(username, teamId, gameId);
                        try {
                                out.writeObject(msgSent);
                                out.reset();
                                MessageFromServer msgReceived = (MessageFromServer) in.readObject();

                                this.roundNumber = msgReceived.getRoundNumber();

                                Question question = msgReceived.getQuestion();
                                List<Score> scoreboard = msgReceived.getScoreboard();
                                this.gui.displayScoreboard(scoreboard);
                                if (question == null) {
                                        this.gui.displayEndGame();
                                        return;
                                }
                                this.gui.displayQuestion(question);
                        } catch (Exception e) {
                                // TODO: handle exception
                                break;
                        }
                }
        }

        private void sendAnswer(int option) {
                MessageFromClient msgSent = MessageFromClient.getAnswerQuestion(username, option, roundNumber);
                System.out.println(roundNumber);
                try {
                        out.writeObject(msgSent);
                        out.reset();
                } catch (Exception e) {
                        gui.displayError("Error sending answer to server.");
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
                        comunicate();
                } catch (IOException e) {
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
                        System.out.println("error closing resources");
                }
        }

}
