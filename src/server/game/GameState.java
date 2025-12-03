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

import configs.Config;

public class GameState implements Runnable {

        private final String id;
        private final Quiz quiz;
        private final TeamManager teamManager;
        private final GameManager gameManager;
        private final Set<DealWithClient> clientHandlers = ConcurrentHashMap.newKeySet();

        private volatile boolean started = false;
        private int curQuestion = -1;

        private ModifiedCountdownLatch latch;
        private AtomicInteger numberOfRealPlayers = new AtomicInteger(0); // not including ones that left mid game
        private Set<String> answeredPlayers = new HashSet<>();

        public GameState(String id, int teamCapacity, int numberOfTeams, Quiz quiz, GameManager gameManager) {
                this.id = id;
                this.teamManager = new TeamManager(numberOfTeams, teamCapacity);
                this.quiz = quiz;
                this.gameManager = gameManager;
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

        public boolean registerPlayer(String username, String teamName, DealWithClient handler) {
                if (started) {
                        Server.getInstance().getLogger()
                                        .info("Game " + id + " has already started unable to add" + username);
                        return false;
                }
                if (!teamManager.registerPlayer(username, teamName)) {
                        Server.getInstance().getLogger().debug("Failed to add to team");
                        return false;
                }
                clientHandlers.add(handler);
                gameManager.startGameWhenFull(id);
                numberOfRealPlayers.incrementAndGet();
                return true;
        }

        public boolean unregisterPlayer(String username) {
                if (teamManager.getPlayer(username) == null)
                        return false;

                DealWithClient toRemove = null;
                for (DealWithClient handler : clientHandlers) {
                        if (handler.getClientId() != username)
                                continue;
                        toRemove = handler;
                        break;
                }

                if (!clientHandlers.remove(toRemove))
                        return false;
                numberOfRealPlayers.decrementAndGet();
                return true;
        }

        public void answer(String username, int option) {
                // hasn't started or does have already answered this question
                if (!started || answeredPlayers.contains(username))
                        return;

                Player player = teamManager.getPlayer(username);
                if (player == null)
                        return;

                Question question = quiz.getQuestion(curQuestion);

                int bonusFactor = latch.countdown();

                if (!question.isCorrect(option))
                        return;

                int updatedScore = player.getScore() + question.getPoints() * bonusFactor;
                player.setScore(updatedScore);

                answeredPlayers.add(username);
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
                while (++curQuestion < numberOfQuestions) {
                        this.latch = new ModifiedCountdownLatch(Config.Features.BONUS_COUNT,
                                        Config.Features.BONUS_FACTOR,
                                        Config.Features.ROUND_SECONDS,
                                        numberOfRealPlayers.get());

                        Question question = quiz.getQuestion(curQuestion);
                        broadcastQuestion(question);
                        try {
                                latch.await();
                        } catch (InterruptedException e) {
                                Server.getInstance().getLogger().error("GameState start latch: unwanted interrupted");
                        }
                        broadcastScoreboard(teamManager.getScoreboard());
                        answeredPlayers.clear(); // reset players who have answered
                }
                endGame();
        }

        private void broadcastQuestion(Question question) {
                broadcast(handler -> handler.sendQuestion(question));
        }

        private void broadcastScoreboard(List<Score> scoreboard) {
                broadcast(handler -> handler.sendScoreboard(scoreboard));
        }

        private void endGame() {
                broadcast(handler -> handler.sendEndGame());
                gameManager.removeGame(id);
        }

        private void broadcast(Consumer<DealWithClient> action) {
                clientHandlers.removeIf(handler -> {
                        try {
                                action.accept(handler);
                                return false;
                        } catch (Exception e) {
                                return true; //remove any invalid handlers
                        }
                });
        }

}
