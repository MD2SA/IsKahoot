package server.game;

import server.DealWithClient;
import server.GameManager;
import server.Server;
import server.game.utils.Lock;
import server.game.utils.ModifiedCountdownLatch;
import server.game.utils.Player;
import server.game.utils.Score;
import utils.quiz.Question;
import utils.quiz.Quiz;

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
        private AtomicInteger numberOfRealPlayers = new AtomicInteger(0);
        // private Set<String> answeredPlayers = new HashSet<>();

        private volatile boolean started = false;
        private AtomicInteger curRound = new AtomicInteger(-1);
        private ModifiedCountdownLatch latch;
        private final Lock scoringLock = new Lock();

        public GameState(String id, int teamCapacity, int numberOfTeams, Quiz quiz, GameManager gameManager) {
                this.id = id;
                this.quiz = quiz;
                this.teamManager = new TeamManager(numberOfTeams, teamCapacity);
                this.gameManager = gameManager;
        }

        @Override
        public void run() {
                started = true;
                Server.getInstance().getLogger().debug("Game started: " + id);

                int round;
                int numberOfQuestions = quiz.getNumberOfQuestions();

                while ((round = curRound.incrementAndGet()) < numberOfQuestions && numberOfRealPlayers.get() > 0) {

                        this.latch = getGameLatch();
                        Question question = quiz.getQuestion(round);
                        broadcastUpdate(question, teamManager.getScoreboard());
                        try {
                                latch.await();
                                scoringLock.lock();
                        } catch (InterruptedException e) {
                                Server.getInstance().getLogger().error("GameState start latch interrupted");
                        } finally {
                                scoringLock.unlock();
                        }

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
                if (!isValidAnswer(username, roundNumber)) {
                        Server.getInstance().getLogger().info("Cannot accept answer from " + username);
                        return;
                }

                Player player = teamManager.getPlayer(username);
                if (player == null)
                        return;

                Question question = quiz.getQuestion(roundNumber);

                try {
                        scoringLock.lock();
                        int bonusFactor = latch.countdown();
                        if (!question.isCorrect(option))
                                return;

                        int updatedScore = player.getScore() + question.getPoints() * bonusFactor;
                        player.setScore(updatedScore);

                        // answeredPlayers.add(username);
                } catch (InterruptedException e) {
                } finally {
                        scoringLock.unlock();
                }

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

        private boolean isValidAnswer(String username, int roundNumber) {
                return started && latch != null && roundNumber == curRound.get()
                                && teamManager.hasPlayer(username);
                // && !answeredPlayers.contains(username);
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

}
