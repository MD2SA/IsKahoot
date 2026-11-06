package server.game;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import configs.Config.Features;
import utils.RoundTimer;
import utils.quiz.Answer;
import utils.quiz.Question;
import utils.quiz.Quiz;
import utils.quiz.RoundResult;
import utils.quiz.Scoreboard;

public class RoundManager implements AutoCloseable {

    private final Quiz quiz;
    private final TeamManager teamManager;
    private final RoundTimer timer = new RoundTimer();
    private Scoreboard scoreboard;

    private int currentRound = -1;
    private RoundState activeRound;
    private long roundDeadlineEpochMillis;

    public RoundManager(Quiz quiz, TeamManager teamManager) {
        this.quiz = quiz;
        this.teamManager = teamManager;
    }

    public synchronized boolean hasNextRound() {
        return currentRound + 1 < quiz.getNumberOfQuestions();
    }

    public synchronized Question prepareNextRound() {
        if (!hasNextRound())
            throw new IllegalStateException("No more rounds");

        currentRound++;
        Question question = quiz.getQuestion(currentRound);
        Map<String, String> roster = rosterSnapshot();
        if (scoreboard == null)
            scoreboard = new Scoreboard(new HashSet<>(roster.values()));
        activeRound = new RoundState(currentRound, question, roster);

        return question;
    }

    public synchronized void openRound() {
        requireActiveRound().open();
    }

    public synchronized long startTimer() {
        RoundState round = requireActiveRound();

        roundDeadlineEpochMillis = System.currentTimeMillis() + Features.ROUND_SECONDS * 1_000L;
        if (round.isOpen()) {
            timer.schedule(Features.ROUND_SECONDS, round::timeout);
        }
        return roundDeadlineEpochMillis;
    }

    public synchronized AnswerStatus answer(String authenticatedUsername, Answer answer) {
        RoundState round = activeRound;
        if (round == null)
            return AnswerStatus.REJECTED;

        AnswerStatus status = round.submitAnswer(authenticatedUsername, answer);
        if (status == AnswerStatus.ROUND_COMPLETED)
            timer.cancel();
        return status;
    }

    public synchronized void playerLeft(String username) {
        if (activeRound != null && activeRound.playerLeft(username))
            timer.cancel();
    }

    public Scoreboard awaitResult() throws InterruptedException, ExecutionException {
        RoundState round;

        synchronized (this) {
            round = requireActiveRound();
        }

        RoundResult result = round.awaitResult();
        scoreboard.applyResult(result);
        return scoreboard;
    }

    public synchronized int getRoundNumber() {
        return currentRound;
    }

    @Override
    public void close() {
        timer.close();
    }

    private RoundState requireActiveRound() {
        if (activeRound == null)
            throw new IllegalStateException("No active round");

        return activeRound;
    }

    private Map<String, String> rosterSnapshot() {
        return teamManager.getRosterSnapshot();
    }
}
