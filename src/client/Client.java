package client;

import gui.GUI;
import gui.Observer.Observer;
import gui.Observer.Observed;

import utils.Question;

import java.util.Arrays;

public class Client extends Thread implements Observer  {

        private final GUI gui;

        public Client() {
                gui = new GUI();
                gui.registerObserver(this);

                // Section just for tests, to delete
                try{
                        Thread.sleep(1000);
                } catch (InterruptedException e ) {}
                gui.displayQuestion(new Question(
                        "Quanto é 2+2?",
                        10,
                        2,
                        Arrays.asList("2","3","4","1")
                ));
        }

        @Override
        public void update(Observed source) {
                if(gui.hasSelectedOption())
                        sendAnswer(gui.getSelectedOption());
        }

        private void sendAnswer(int option) {
                /** TODO:
                 *  - [ ] send option to server
                 *
                 */
        }
}
