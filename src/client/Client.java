package client;

import client.gui.GUI;
import client.gui.Observer.Observer;
import client.gui.Observer.Observed;

import configs.Config;
import server.Server;
import utils.Quiz;
import utils.QuizCreator;

import java.io.File;
import java.io.IOException;

public class Client extends Thread implements Observer  {

        private final GUI gui;
        private final Server server;
        private final String username;

        public Client(Server server, String username) {
                this.server = server;
                this.username = username;

                gui = new GUI();
                gui.registerObserver(this);

                // just for testing client.gui
                test();

        }

        public void getQuestion() {
                /*
                here we should ask the server in a synchronized way for the new question
                something like
                Question question = server.getQuestion()
                client.gui.displayQuestion(question);
                 */
        }

        private void sendAnswer(int option) {
                server.answerFrom(option, username);
                System.out.println("SENDING ANSWER "+option);
                /* TODO:
                 *  - [ ] send option to server
                 *
                 */
        }


        @Override
        public void update(Observed source) {
                if(gui.hasSelectedOption())
                        sendAnswer(gui.getSelectedOption());
        }

        // Section just for tests, to delete
        private void test() {
                try {
                        new Thread(()-> {
                                try {
                                        File quizzesFile = new File("quizzes.json");
                                        Quiz quiz  = QuizCreator.getFrom(quizzesFile).getFirst();

                                        while(quiz.hasNextQuestion()) {
                                                gui.update(quiz.nextQuestion(), null);
                                                sleep(1000*Config.Features.ROUND_SECONDS);
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
