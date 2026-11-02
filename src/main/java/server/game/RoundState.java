package server.game;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import server.game.utils.Scorer;
import utils.quiz.Answer;
import utils.quiz.Question;
import utils.quiz.RoundResult;

public class RoundState {

    private enum Phase {
        PREPARED, OPEN, CLOSED
    }

    private final int round;
    private final Question question;
    private final Map<String, String> roster;
    private final Map<String, Answer> answers = new LinkedHashMap<>();
    private final CompletableFuture<RoundResult> result = new CompletableFuture<>();

    private Phase phase = Phase.PREPARED;

    public RoundState(int round, Question question, Map<String, String> roster) {
        this.round = round;
        this.question = question;
        this.roster = Map.copyOf(roster);
    }

    synchronized void open() {
        if (phase != Phase.PREPARED)
            throw new IllegalStateException("Round is not prepared");

        phase = Phase.OPEN;
    }

    synchronized boolean isOpen() {
        return phase == Phase.OPEN;
    }

    boolean submitAnswer(String username, Answer answer) {
        Map<String, Answer> snapshot = null;

        synchronized (this) {
            String teamId = roster.get(username);

            if (phase != Phase.OPEN || answer == null || answer.getRoundNumber() != round || teamId == null
                    || !teamId.equals(answer.getTeamId()) || answers.containsKey(username))
                return false;

            answers.put(username, answer);

            if (answers.size() == roster.size()) {
                phase = Phase.CLOSED;
                snapshot = Map.copyOf(answers);
            }
        }

        if (snapshot != null)
            completeResult(snapshot);

        return snapshot != null;
    }

    void timeout() {
        Map<String, Answer> snapshot;

        synchronized (this) {
            if (phase != Phase.OPEN)
                return;

            phase = Phase.CLOSED;
            snapshot = Map.copyOf(answers);
        }

        completeResult(snapshot);
    }

    private void completeResult(Map<String, Answer> snapshot) {
        try {
            result.complete(Scorer.score(question, roster, snapshot));
        } catch (RuntimeException e) {
            result.completeExceptionally(e);
        }
    }

    RoundResult awaitResult() throws InterruptedException, ExecutionException {
        return result.get();
    }
}
