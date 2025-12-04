package server.game;

import server.DealWithClient;
import server.GameManager;
import server.Server;
import server.game.utils.ModifiedCountdownLatch;
import server.game.utils.Player;
import server.game.utils.Score;
import utils.quiz.Question;
import utils.quiz.Quiz;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import configs.Config;

public class GameState implements Runnable {

        private final String id;
        private final Quiz quiz;
        private final TeamManager teamManager;
        private final GameManager gameManager;

        private final Set<DealWithClient> clientHandlers = ConcurrentHashMap.newKeySet();
        private AtomicInteger numberOfRealPlayers = new AtomicInteger(0); // not including ones that left mid game
        private Set<String> answeredPlayers = new HashSet<>();

        private volatile boolean started = false;
        private int curRound = -1;
        private List<ModifiedCountdownLatch> latchs;

        public GameState(String id, int teamCapacity, int numberOfTeams, Quiz quiz, GameManager gameManager) {
                this.id = id;
                this.quiz = quiz;
                this.teamManager = new TeamManager(numberOfTeams, teamCapacity);
                this.gameManager = gameManager;
        }

        @Override
        public void run() {
                synchronized (this) {
                        if (started)
                                return;
                        started = true;
                        Server.getInstance().getLogger().debug("Game started: " + id);
                }

                int numberOfQuestions = quiz.getNumberOfQuestions();
                latchs = IntStream.range(0, numberOfQuestions).mapToObj(i -> getGameLatch()).toList();

                while (++curRound < numberOfQuestions) {
                        ModifiedCountdownLatch latch = latchs.get(curRound);

                        Question question = quiz.getQuestion(curRound);
                        broadcastUpdate(question, teamManager.getScoreboard());
                        try {
                                latch.await();
                        } catch (InterruptedException e) {
                                Server.getInstance().getLogger().error("GameState start latch: unwanted interrupted");
                        }
                        answeredPlayers.clear(); // reset players who have answered
                }
                endGame();
        }

        public boolean registerPlayer(String username, String teamName, DealWithClient handler) {
                if (started || !teamManager.registerPlayer(username, teamName))
                        return false;

                clientHandlers.add(handler);
                numberOfRealPlayers.incrementAndGet();
                gameManager.startGameWhenFull(id);

                return true;
        }

        public boolean unregisterPlayer(String username) {
                DealWithClient handler = clientHandlers.stream()
                                .filter(h -> h.getClientId().equals(username))
                                .findFirst()
                                .orElse(null);

                if (handler == null || !clientHandlers.remove(handler))
                        return false;
                if (started)
                        teamManager.remove(username);
                numberOfRealPlayers.decrementAndGet();

                return true;
        }

        public void answer(String username, int roundNumber, int option) {
                // hasn't started or does have already answered this question
                if (!started || roundNumber < 0 || roundNumber >= latchs.size() || answeredPlayers.contains(username))
                        return;

                Player player = teamManager.getPlayer(username);
                if (player == null)
                        return;

                Question question = quiz.getQuestion(roundNumber);

                int bonusFactor = latchs.get(roundNumber).countdown();

                if (!question.isCorrect(option))
                        return;

                int updatedScore = player.getScore() + question.getPoints() * bonusFactor;
                player.setScore(updatedScore);

                answeredPlayers.add(username);
        }


        private void broadcastUpdate(Question question, List<Score> scoreboard) {
                broadcast(handler -> handler.sendRoundInfo(question, scoreboard));
        }

        private void endGame() {
                List<Score> scoreboard = teamManager.getScoreboard(); // keep outside sendEndGame
                broadcast(handler -> handler.sendEndGame(scoreboard));
                gameManager.removeGame(id);
        }

        private void broadcast(Consumer<DealWithClient> action) {
                clientHandlers.removeIf(handler -> {
                        try {
                                action.accept(handler);
                                return false;
                        } catch (Exception e) {
                                return true; // remove any invalid handlers
                        }
                });
        }

        private ModifiedCountdownLatch getGameLatch() {
                return new ModifiedCountdownLatch(Config.Features.BONUS_COUNT,
                                Config.Features.BONUS_FACTOR,
                                Config.Features.ROUND_SECONDS,
                                numberOfRealPlayers.get());
        }

        public String getId() {
                return id;
        }

        public boolean isFull() {
                return teamManager.isFull();
        }

        public boolean hasStarted() {
                return started;
        }
}
