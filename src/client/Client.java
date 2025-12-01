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

        private ObjectOutputStream out;
        private ObjectInputStream in;
        private Socket socket;

        public Client(String username, String teamId, String gameId) {
                this.username = username;
                this.gameId = gameId;
                this.teamId = teamId;

                gui = new GUI(username, teamId);
                gui.registerObserver(this);

        }

        public void getQuestion() {
                while (true) {
                        MessageFromClient msgSent = new MessageFromClient(Code.GET_QUESTION, username, teamId, gameId);
                        try {
                                out.writeObject(msgSent);
                                out.reset();
                                MessageFromServer msgReceived = (MessageFromServer) in.readObject();
                                Question question = msgReceived.getQuestion();
                                List<Score> scoreboard = msgReceived.getScoreboard();
                                if (question != null) {
                                        this.gui.displayQuestion(question);
                                } else {
                                        this.gui.displayScoreboard(scoreboard);
                                }
                        } catch (Exception e) {
                                // TODO: handle exception
                        }
                }
        }

        private void sendAnswer(int option) {
                MessageFromClient msgSent = new MessageFromClient(Code.POST_ANSWER, username, teamId, gameId, option);
                try {
                        out.writeObject(msgSent);
                        out.reset();
                        System.out.println("SENDING"+option);
                } catch (Exception e) {
                        System.out.println("ERROR SENDING ASNWER");
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
                        getQuestion();
                } catch (IOException e) {
                } finally {
                        try {
                                socket.close();
                        } catch (Exception e) {

                        }
                }
        }

        void connectToServer() throws IOException {
                InetAddress addr = InetAddress.getByName(null);
                socket = new Socket(addr, Config.Server.SERVER_PORT);
                this.out = new ObjectOutputStream(socket.getOutputStream());
                this.in = new ObjectInputStream(socket.getInputStream());
        }

}
