package server.game;

import java.util.List;
import java.util.function.Consumer;

import server.DealWithClient;
import server.Server;
import utils.quiz.Answer;
import utils.quiz.Question;
import utils.quiz.Quiz;
import utils.quiz.Score;
import utils.tools.ConcurrentSet;

public class GameSession implements Runnable {

        private final String id;
        private final GameState gameState;
        private final SyncController syncController;

        private final ConcurrentSet<DealWithClient> clientHandlers = new ConcurrentSet<>();

        public GameSession(String id, Quiz quiz, int numberOfTeams, int teamCapacity) {
                this.id = id;
                this.gameState = new GameState(quiz, numberOfTeams, teamCapacity);
                this.syncController = new SyncController();
        }

        public boolean join(DealWithClient handler) {
                if (!gameState.registerPlayer(handler.getClientId(), handler.getTeamId()))
                        return false;
                clientHandlers.add(handler);
                return true;
        }

        public boolean leave(DealWithClient handler) {
                clientHandlers.remove(handler);
                gameState.remove(handler.getClientId());
                return true;
        }

        public void receiveAnswer(Answer answer) {
                if (syncController.isRoundReady())
                        gameState.answer(answer, syncController);
                System.out.println("IS ROUND READY" + syncController.isRoundReady());
        }

        @Override
        public void run() {
                Server.getInstance().getLogger().info("Game " + id + " started.");
                int rounds = gameState.getQuiz().getNumberOfQuestions();
                for (int round = 0; round < rounds; round++) {
                        Question question = gameState.getQuiz().getQuestion(round);
                        syncController.prepareRound(round, question.isIndividual(), clientHandlers.size(),
                                        gameState.getTeams());

                        broadcastUpdate(question, gameState.getScoreboard());

                        syncController.waitForRoundEnd();
                        ScoreEvaluator.scoreAnswers(question, gameState.getAnswers());
                }
                endGame();
        }

        private void broadcastUpdate(Question question, List<Score> scoreboard) {
                broadcast(handler -> handler.sendRoundInfo(question, scoreboard));
        }

        private void endGame() {
                List<Score> scoreboard = gameState.getScoreboard(); // keep outside
                broadcast(handler -> handler.sendEndGame(scoreboard));
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

        public String getId() {
                return id;
        }

        public boolean isFull() {
                return gameState.isFull();
        }
}
