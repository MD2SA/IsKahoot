package server.game;

import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    private final Set<String> departedPlayers = new HashSet<>();
    private final CompletableFuture<RoundResult> result = new CompletableFuture<>();

    private Phase phase = Phase.PREPARED;

    public RoundState(int round, Question question, Map<String, String> roster) {
        this.round = round;
        this.question = question;
        this.roster = Map.copyOf(roster);
    }

    void open() {
        Map<String, Answer> snapshot = null;
        synchronized (this) {
            if (phase != Phase.PREPARED)
                throw new IllegalStateException("Round is not prepared");

            if (departedPlayers.size() == roster.size()) {
                phase = Phase.CLOSED;
                snapshot = Map.of();
            } else {
                phase = Phase.OPEN;
            }
        }

        if (snapshot != null)
            completeResult(snapshot);
    }

    synchronized boolean isOpen() {
        return phase == Phase.OPEN;
    }

    AnswerStatus submitAnswer(String username, Answer answer) {
        Map<String, Answer> snapshot = null;

        synchronized (this) {
            if (!acceptsAnswer(username, answer))
                return AnswerStatus.REJECTED;

            answers.put(username, answer);

            if (answers.size() + departedPlayers.size() == roster.size()) {
                phase = Phase.CLOSED;
                snapshot = new LinkedHashMap<>(answers);
            }
        }

        if (snapshot != null)
            completeResult(snapshot);

        return snapshot != null ? AnswerStatus.ROUND_COMPLETED : AnswerStatus.ACCEPTED;
    }

    private boolean acceptsAnswer(String username, Answer answer) {
        if (phase != Phase.OPEN || username == null || answer == null || answers.containsKey(username)
                || departedPlayers.contains(username))
            return false;

        String teamId = roster.get(username);
        int option = answer.getOption();
        return teamId != null && answer.getRoundNumber() == round && option >= 0
                && option < question.getOptions().size() && teamId.equals(answer.getTeamId())
                && username.equals(answer.getUsername());
    }

    boolean playerLeft(String username) {
        Map<String, Answer> snapshot = null;

        synchronized (this) {
            if (phase == Phase.CLOSED || username == null || !roster.containsKey(username)
                    || answers.containsKey(username) || !departedPlayers.add(username))
                return false;

            if (phase == Phase.PREPARED)
                return false;

            if (answers.size() + departedPlayers.size() == roster.size()) {
                phase = Phase.CLOSED;
                snapshot = new LinkedHashMap<>(answers);
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
            snapshot = new LinkedHashMap<>(answers);
        }

        completeResult(snapshot);
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
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
