package server.game;

import configs.Config.Features;

import utils.quiz.Answer;
import utils.quiz.Question;
import server.game.utils.Player;
import utils.quiz.Quiz;
import server.game.utils.Team;
import utils.tools.ConcurrentMap;
import server.game.utils.ModifiedCountdownLatch;
import server.game.utils.ModifiedCyclicBarrier;

import java.util.Map;

public class RoundManager {

    private final Quiz quiz;
    private int currentQuestionIndex = -1;
    private final TeamManager teamManager;
    private final ConcurrentMap<String, Answer> answers = new ConcurrentMap<>();
    private ModifiedCountdownLatch globalLatch;
    private Map<String, ModifiedCyclicBarrier> teamBarriers;
    private boolean roundReady;
    private boolean closing;
    private int activeAnswerCount;

    public RoundManager(Quiz quiz, TeamManager teamManager) {
        this.quiz = quiz;
        this.teamManager = teamManager;
    }

    public synchronized Question nextQuestion() {
        currentQuestionIndex++;
        Question question = quiz.getQuestion(currentQuestionIndex);

        this.globalLatch = new ModifiedCountdownLatch(Features.BONUS_FACTOR, Features.BONUS_COUNT,
                Features.ROUND_SECONDS, teamManager.getUsernames().size());
        if (!question.isIndividual()) {
            teamBarriers = new ConcurrentMap<>();
            teamManager.getTeams().values().forEach(team -> {
                teamBarriers.put(team.getName(), new ModifiedCyclicBarrier(team.size(), getBarrierAction(team)));
            });
        }

        answers.clear();
        activeAnswerCount = 0;
        closing = false;
        roundReady = true;

        return question;
    }

    public void answer(Answer answer) throws InterruptedException {
        synchronized (this) {
            if (!roundReady)
                return;
            activeAnswerCount++;
        }

        try {
            if (!isValidAnswer(answer))
                return;

            answers.put(answer.getUsername(), answer);
            Question question = getCurrentQuestion();
            Team team = teamManager.getTeams().get(answer.getTeamId());
            if (team == null)
                return;

            if (question.isIndividual())
                handleIndividual(team, question, answer);
            else
                handleTeam(answer);
        } finally {
            synchronized (this) {
                activeAnswerCount--;
                if (closing && activeAnswerCount == 0)
                    notifyAll();
            }
        }
    }

    public void waitForRoundEnd() throws InterruptedException {
        globalLatch.await();

        synchronized (this) {
            roundReady = false;
            closing = true;
        }

        if (!getCurrentQuestion().isIndividual()) {
            teamBarriers.values().forEach(ModifiedCyclicBarrier::forceRelease);
        }

        synchronized (this) {
            while (activeAnswerCount > 0)
                wait();
            closing = false;
        }
    }

    private boolean isValidAnswer(Answer answer) {
        return answer.getRoundNumber() == currentQuestionIndex
                && teamManager.getUsernames().contains(answer.getUsername())
                && !answers.containsKey(answer.getUsername());
    }

    private void handleIndividual(Team team, Question question, Answer answer) {
        int bonus = globalLatch.countdown();
        if (question.isCorrect(answer.getOption()))
            team.increaseScore(bonus * question.getPoints());
    }

    private void handleTeam(Answer answer) throws InterruptedException {
        ModifiedCyclicBarrier barrier = teamBarriers.get(answer.getTeamId());
        if (barrier != null)
            barrier.await();
        globalLatch.countdown();
    }

    public Question getCurrentQuestion() {
        return quiz.getQuestion(currentQuestionIndex);
    }

    public boolean hasNextQuestion() {
        return quiz.getNumberOfQuestions() > currentQuestionIndex + 1;
    }

    public boolean isRoundReady() {
        return roundReady;
    }

    public int getRoundNumber() {
        return currentQuestionIndex;
    }

    private Runnable getBarrierAction(Team team) {
        return () -> {
            Question question = getCurrentQuestion();
            boolean allCorrect = true;
            int bestScore = 0;

            for (Player p : team.getPlayers()) {
                Answer answer = answers.get(p.getName());
                if (answer == null || !question.isCorrect(answer.getOption())) {
                    allCorrect = false;
                } else {
                    bestScore = Math.max(bestScore, question.getPoints());
                }
            }

            team.increaseScore(allCorrect ? bestScore * 2 : bestScore);
        };
    }
}
