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
import utils.quiz.Quiz;
import utils.quiz.QuizCreator;

import java.io.File;
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

                gui = new GUI();
                gui.registerObserver(this);

        }

        public void getQuestion() {
                while (true) {
                        MessageFromClient msgSent = new MessageFromClient(Code.GET_QUESTION, username, teamId, gameId);
                        try {
                                System.out.println("Sending");
                                out.writeObject(msgSent);
                                System.out.println("Sent");
                                MessageFromServer msgReceived = (MessageFromServer) in.readObject();
                                Question question = msgReceived.getQuestion();
                                List<Score> scoreboard = msgReceived.getScoreboard();
                                if (question != null)
                                        this.gui.update(question, null);
                                else
                                        this.gui.update(null,scoreboard);
                        } catch (Exception e) {
                                // TODO: handle exception
                        }
                }
                /*
                 * here we should ask the server in a synchronized way for the new question
                 * something like
                 * Question question = server.getQuestion()
                 * client.gui.displayQuestion(question);
                 */
        }

        private void sendAnswer(int option) {
                System.out.println("SENDING ANSWER " + option);
                /*
                 * TODO:
                 * - [ ] send option to server
                 *
                 */
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

        // Section just for tests, to delete
        private void test() {
                try {
                        new Thread(() -> {
                                try {
                                        File quizzesFile = new File("quizzes.json");
                                        Quiz quiz = QuizCreator.getFrom(quizzesFile).getFirst();

                                        while (quiz.hasNextQuestion()) {
                                                gui.update(quiz.nextQuestion(), null);
                                                sleep(1000 * Config.Features.ROUND_SECONDS);
                                        }
                                        gui.update(null, null);

                                } catch (IOException | InterruptedException e) {
                                        throw new RuntimeException(e);
                                }
                        }).start();
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
        }
}
