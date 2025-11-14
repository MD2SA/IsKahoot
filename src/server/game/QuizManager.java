package server.game;

import server.game.utils.ModifiedCountdownLatch;
import server.game.utils.Player;
import utils.Question;
import utils.Quiz;

public class QuizManager {

        private ModifiedCountdownLatch latch;
        private Quiz quiz;
        private final boolean isMultiPlayer;

        public QuizManager(Quiz quiz) {
                this(quiz, false);
        }

        public QuizManager(Quiz quiz, boolean isMultiPlayer) {
                this.quiz = quiz;
                this.isMultiPlayer = isMultiPlayer;
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
