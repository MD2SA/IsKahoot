package server.game;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import server.DealWithClient;
import utils.quiz.Answer;
import utils.quiz.Question;
import utils.quiz.Quiz;
import utils.quiz.ReducedQuestion;
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
            Question question = roundManager.nextQuestion();
            broadcastRoundUpdate(question, getScoreboard());
            try {
                roundManager.waitForRoundEnd();
            } catch (InterruptedException e) {
                break;
            }
        }
        endGame();
    }

    private void broadcastRoundUpdate(Question question, List<Score> scoreboard) {
        ReducedQuestion reducedQuestion = new ReducedQuestion(question, roundManager.getRoundNumber());
        broadcast(handler -> handler.sendRoundInfo(reducedQuestion, scoreboard));
    }

    private void endGame() {
        List<Score> scoreboard = getScoreboard();
        broadcast(handler -> {
            handler.sendEndGame(scoreboard);
            handler.close();
        });
    }

    private void broadcast(Consumer<DealWithClient> action) {
        clientHandlers.removeIf(handler -> {
            try {
                action.accept(handler);
                return false;
            } catch (Exception e) {
                return true;
            }
        });
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
