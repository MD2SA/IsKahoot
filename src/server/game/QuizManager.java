package server.game;

import server.game.utils.ModifiedCountdownLatch;
import server.game.utils.Player;
import utils.Question;
import utils.Quiz;

import java.util.Objects;

public class QuizManager {

        private ModifiedCountdownLatch latch;
        private Quiz quiz;
        private final boolean isIndividualQuestion;

        public QuizManager(Quiz quiz) {
                this(quiz, false);
        }

        public QuizManager(Quiz quiz, boolean isIndividualQuestion) {
                this.quiz = quiz;
                this.isIndividualQuestion = isIndividualQuestion;
        }

        public Question nextQuestion() {
                Question question = quiz.nextQuestion();
                this.latch = new ModifiedCountdownLatch(2,2,5,10);
                return question;
        }

        public void submitAnswer(Player player, int option) {
                if(quiz == null || latch == null) throw new IllegalStateException();

                Question question = quiz.getCurQuestion();

                int bonusFactor;
                try {
                        bonusFactor = latch.countdown();
                } catch (InterruptedException e) {
                        bonusFactor = 1;
                }

                if(!question.isCorrect(option)) return;
                int updatedScore = player.getScore() + question.getPoints()*bonusFactor;
                player.setScore(updatedScore);
        }

        public ModifiedCountdownLatch latch() { return latch; }

}
