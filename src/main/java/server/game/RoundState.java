package server.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import server.game.utils.Phase;
import server.game.utils.Scorer;
import utils.quiz.Answer;
import utils.quiz.Question;
import utils.quiz.RoundResult;

public class RoundState {

    private Phase phase;
    private final int round;
    private final Question question;
    private final Set<String> roster;
    private final Map<String, Answer> answers = new HashMap<>();
    private final CompletableFuture<RoundResult> finished = new CompletableFuture<>();

    public RoundState(int round, Question question, Set<String> roster) {
        this.round = round;
        this.roster = roster;
        this.question = question;
        phase = Phase.OPEN;
    }

    void submitAnswer(String username, int round, Answer answer) {
        synchronized (this) {
            if (phase != Phase.OPEN || round != this.round || !roster.contains(username)
                    || answers.containsKey(username))
                return;
            answers.put(username, answer);
        }

        if (allAnswered())
            finishRound();
    }

    private boolean allAnswered() {
        return answers.size() == roster.size();
    }

    private void finishRound() {
        synchronized (this) {
            if (phase != Phase.OPEN)
                return;
            phase = Phase.CLOSED;
        }

        RoundResult result = Scorer.score(question, answers);
        finished.complete(result);
    }

    public RoundResult getRoundResult() throws InterruptedException, ExecutionException {
        return finished.get();
    }

}
