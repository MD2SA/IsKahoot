package server.game;

import server.game.utils.ModifiedCountdownLatch;
import server.game.utils.Player;
import utils.quiz.Question;
import utils.quiz.Quiz;

public class QuizManager {

        private ModifiedCountdownLatch latch;
        private Quiz quiz;

        public QuizManager(Quiz quiz) {
                this.quiz = quiz;
        }

        public Question nextQuestion() {
                Question question = quiz.nextQuestion();
                this.latch = new ModifiedCountdownLatch(2,2,5,10);
                return question;
        }

        public void submitAnswer(Player player, int option) {
                if(quiz == null || latch == null) throw new IllegalStateException();

                Question question = quiz.getCurQuestion();

                int bonusFactor = latch.countdown();

                if(!question.isCorrect(option)) return;
                int updatedScore = player.getScore() + question.getPoints()*bonusFactor;
                player.setScore(updatedScore);
        }

        public void awaitFinish() {
                try {
                        latch.await();
                } catch (InterruptedException e) {}
        }

}
