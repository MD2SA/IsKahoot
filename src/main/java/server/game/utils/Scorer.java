package server.game.utils;

import java.util.HashMap;
import java.util.Map;

import configs.Config.Features;
import utils.quiz.Answer;
import utils.quiz.Question;
import utils.quiz.RoundResult;

public class Scorer {

    private Scorer() {
    }

    public static RoundResult score(Question question, Map<String, String> roster, Map<String, Answer> answers) {
        return question.isIndividual()
                ? scoreIndividual(question, roster, answers)
                : scoreTeam(question, roster, answers);
    }

    private static RoundResult scoreIndividual(Question question, Map<String, String> roster,
            Map<String, Answer> answers) {
        RoundResult result = new RoundResult();
        int answerPosition = 0;

        for (var entry : answers.entrySet()) {
            int multiplier = answerPosition++ < Features.BONUS_COUNT ? Features.BONUS_FACTOR : 1;
            Answer answer = entry.getValue();

            if (question.isCorrect(answer.getOption()))
                result.increaseScore(question.getPoints() * multiplier, roster.get(entry.getKey()));
        }

        return result;
    }

    private static RoundResult scoreTeam(Question question, Map<String, String> roster, Map<String, Answer> answers) {
        Map<String, Boolean> allCorrectByTeam = new HashMap<>();
        Map<String, Integer> bestScoreByTeam = new HashMap<>();

        for (var participant : roster.entrySet()) {
            String username = participant.getKey();
            String teamId = participant.getValue();

            allCorrectByTeam.putIfAbsent(teamId, true);
            bestScoreByTeam.putIfAbsent(teamId, 0);

            Answer answer = answers.get(username);
            if (answer == null || !question.isCorrect(answer.getOption()))
                allCorrectByTeam.put(teamId, false);
            else
                bestScoreByTeam.put(teamId, Math.max(bestScoreByTeam.get(teamId), question.getPoints()));
        }

        RoundResult result = new RoundResult();
        for (var team : allCorrectByTeam.entrySet()) {
            String teamId = team.getKey();
            int bestScore = bestScoreByTeam.get(teamId);
            int score = team.getValue() ? bestScore * Features.BONUS_FACTOR : bestScore;

            if (score > 0)
                result.increaseScore(score, teamId);
        }

        return result;
    }
}
