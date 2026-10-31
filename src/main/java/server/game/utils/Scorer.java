package server.game.utils;

import java.util.Map;

import utils.quiz.Answer;
import utils.quiz.Question;
import utils.quiz.RoundResult;

public class Scorer {

    public static RoundResult score(Question question, Map<String, Answer> answers) {
        return new RoundResult();
    }
    // private Runnable getBarrierAction(Team team) {
    // return () -> {
    // Question question = getCurrentQuestion();
    // boolean allCorrect = true;
    // int bestScore = 0;
    //
    // for (Player p : team.getPlayers()) {
    // Answer answer = answers.get(p.getName());
    // if (answer == null || !question.isCorrect(answer.getOption())) {
    // allCorrect = false;
    // } else {
    // bestScore = Math.max(bestScore, question.getPoints());
    // }
    // }
    //
    // team.increaseScore(allCorrect ? bestScore * 2 : bestScore);
    // };
    // }
}
