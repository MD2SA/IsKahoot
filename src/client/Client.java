package client;

import gui.GUI;
import gui.Observer.Observer;
import gui.Observer.Observed;

import server.Server;
import utils.Question;
import utils.Quiz;
import utils.QuizCreator;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Client extends Thread implements Observer  {

        private final GUI gui;
        private final Server server;
        private final UUID uuid = UUID.randomUUID();

        public Client(Server server) {
                this.server = server;

                gui = new GUI();
                gui.registerObserver(this);

                // Section just for tests, to delete
                try {
                        new Thread(()-> {
                                try {
                                        File quizzesFile = new File("quizzes.json");
                                        Quiz quiz  = QuizCreator.getFrom(quizzesFile).getFirst();

                                        while(quiz.hasNextQuestion()) {
                                                gui.displayQuestion(quiz.nextQuestion());
                                                sleep(1000*3);
                                        }
                                        gui.displayScoreboard(null);

                                } catch (IOException | InterruptedException e) {
                                        throw new RuntimeException(e);
                                }
                        }).start();
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }

        }

        public void getQuestion() {
                /*
                here we should ask the server in a synchronized way for the new question
                something like
                Question question = server.getQuestion()
                 */
                Question question = new Question(
                        "O que é uma ThreadPool...",
                        10,
                        2,
                        Arrays.asList("A","B","C","D")
                );
                gui.displayQuestion(question);
        }

        private void sendAnswer(int option) {
                server.answerFrom(option, uuid);
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

}
