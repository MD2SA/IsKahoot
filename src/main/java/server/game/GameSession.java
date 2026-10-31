package server.game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import server.DealWithClient;
import server.Server;
import utils.quiz.Answer;
import utils.quiz.Question;
import utils.quiz.Quiz;
import utils.quiz.ReducedQuestion;
import utils.quiz.RoundResult;
import utils.quiz.Score;
import utils.tools.ConcurrentSet;

public class GameSession implements Runnable {

    private final String id;
    private final TeamManager teamManager;
    private final RoundManager roundManager;
    private final ConcurrentSet<DealWithClient> clientHandlers = new ConcurrentSet<>();

    public GameSession(String id, Quiz quiz, int numberOfTeams, int teamCapacity) {
        this.id = id;
        this.teamManager = new TeamManager(numberOfTeams, teamCapacity);
        this.roundManager = new RoundManager(quiz, teamManager);
    }

    public boolean join(DealWithClient handler) {
        if (!teamManager.registerPlayer(handler.getClientId(), handler.getTeamId()))
            return false;
        clientHandlers.add(handler);
        return true;
    }

    public boolean leave(DealWithClient handler) {
        clientHandlers.remove(handler);
        teamManager.removePlayer(handler.getClientId());
        return true;
    }

    public void receiveAnswer(Answer answer) throws InterruptedException {
        roundManager.answer(answer);
    }

    @Override
    public void run() {
        while (roundManager.hasNextQuestion() && !clientHandlers.isEmpty()) {
            RoundState round = roundManager.openRound();
            broadcastQuestion(roundManager.getCurrentQuestion());

            try {
                RoundResult result = round.getRoundResult();
                broadcastResult(result);
            } catch (InterruptedException | ExecutionException e) {
                Server.getInstance().getLogger().warn("Unexpected exception while waiting for results");
            }
        }
        endGame();
    }

    private void broadcastQuestion(Question question) {
        ReducedQuestion reducedQuestion = new ReducedQuestion(question, roundManager.getRoundNumber());
        broadcast(handler -> handler.sendQuestion(reducedQuestion));
    }

    private void broadcastResult(RoundResult result) {
        broadcast(handler -> handler.sendResult(result));
    }

    private void endGame() {
        broadcast(handler -> {
            handler.sendEndGame();
            handler.close();
        });
    }

    private void broadcast(Consumer<DealWithClient> action) {
        for (DealWithClient handler : clientHandlers)
            action.accept(handler);
    }

    public List<Score> getScoreboard() {
        return new ArrayList<>(teamManager.getTeams().values());
    }

    public boolean isFull() {
        return teamManager.isFull();
    }

    public String getId() {
        return id;
    }
}
