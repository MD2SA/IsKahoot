package server.game;

import java.util.Map;

import server.game.utils.Answer;
import server.game.utils.Player;
import utils.quiz.Question;

public class ScoreEvaluator {

        static void scoreAnswers(Question question, Map<Player, Answer> answerMap) {
                int points = question.getPoints();
                int correct = question.getCorrect();
                for (Map.Entry<Player, Answer> entry : answerMap.entrySet()) {
                        Player player = entry.getKey();
                        Answer answer = entry.getValue();
                        if (correct != answer.getOption())
                                continue;
                        player.increaseScore(answer.getBonus() * points);
                        System.out.println("Player got it right" + player.getName());
                        System.out.println(answer.getBonus());
                }
        }
}
