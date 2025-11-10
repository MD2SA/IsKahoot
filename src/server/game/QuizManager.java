package server.game;

import server.game.utils.ModifiedCountdownLatch;
import server.game.utils.Player;
import utils.Question;
import utils.Quiz;

import java.util.Objects;

public class QuizManager {

        private ModifiedCountdownLatch latch;
        private Quiz quiz;

        public void startQuiz(Quiz quiz) {
                this.quiz = Objects.requireNonNull(quiz);
        }

        public Question nextQuestion() {
                Question question = quiz.nextQuestion();
                this.latch = new ModifiedCountdownLatch(2,2,5,10);
                latch.startTimer();
                return question;
        }

        public synchronized void submitAnswer(Player player, int option) {
                if(quiz == null || latch == null) throw new IllegalStateException();

                Question question = quiz.getCurQuestion();

                int bonusFactor = latch.countdown();

                if(!question.isCorrect(option)) return;
                int updatedScore = player.getScore() + question.getPoints()*bonusFactor;
                player.setScore(updatedScore);
        }

}
