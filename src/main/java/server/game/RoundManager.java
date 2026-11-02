package server.game;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import configs.Config.Features;
import server.game.utils.Player;
import server.game.utils.Team;
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
    private final Scoreboard scoreboard = new Scoreboard();

    private int currentRound = -1;
    private RoundState activeRound;

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
        activeRound = new RoundState(currentRound, question, rosterSnapshot());

        return question;
    }

    public synchronized void openRound() {
        requireActiveRound().open();
    }

    public synchronized void startTimer() {
        RoundState round = requireActiveRound();

        if (round.isOpen())
            timer.schedule(Features.ROUND_SECONDS, round::timeout);
    }

    public synchronized void answer(String authenticatedUsername, Answer answer) {
        RoundState round = requireActiveRound();

        if (round.submitAnswer(authenticatedUsername, answer))
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
        Map<String, String> roster = new HashMap<>();

        for (Team team : teamManager.getTeams().values()) {
            for (Player player : team.getPlayers())
                roster.put(player.getName(), team.getName());
        }

        return Map.copyOf(roster);
    }
}
