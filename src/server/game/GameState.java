package server.game;

import server.DealWithClient;
import server.GameManager;
import server.Server;
import server.game.utils.Answer;
import server.game.utils.ModifiedCountdownLatch;
import server.game.utils.ModifiedCyclicBarrier;
import server.game.utils.Player;
import utils.quiz.Score;
import server.game.utils.Team;
import utils.quiz.Question;
import utils.quiz.Quiz;
import utils.tools.ConcurrentMap;

import java.util.List;
import java.util.Map;
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
        // private Set<String> answeredPlayers = new HashSet<>();

        private volatile boolean started = false;
        private AtomicInteger curRound = new AtomicInteger(-1);
        private ModifiedCountdownLatch latch;
        // teamId->CyclicBarrier
        private ConcurrentMap<String, ModifiedCyclicBarrier> teamBarriers = new ConcurrentMap<>();

        private final ConcurrentMap<Answer, Integer> answers = new ConcurrentMap<>();

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

                while ((round = curRound.incrementAndGet()) < numberOfQuestions && clientHandlers.size() > 0) {

                        this.latch = getGameLatch();
                        Question question = quiz.getQuestion(round);
                        if (!question.isIndividual()) {
                                teamBarriers = null;
                                teamBarriers = new ConcurrentMap<>();
                                for (Team team : teamManager.getTeams()) {
                                        teamBarriers.put(team.getName(), new ModifiedCyclicBarrier(team.size()));
                                        System.out.println("creating a team barrier witch size " + team.size());
                                }
                        }
                        broadcastUpdate(question, teamManager.getScoreboard());
                        try {
                                latch.await();
                                System.out.println("Stopped waiting");
                        } catch (InterruptedException e) {
                                Server.getInstance().getLogger().error("GameState start latch interrupted");
                        }
                        scoreAnswers();
                }

                endGame();
        }

        public boolean registerPlayer(String username, String teamName, DealWithClient handler) {
                if (started || !teamManager.registerPlayer(username, teamName))
                        return false;

                clientHandlers.add(handler);
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
                if (!started)
                        teamManager.remove(username);

                return true;
        }

        public void answer(Answer answer) {
                if (!isValidAnswer(answer)) {
                        Server.getInstance().getLogger().info("Cannot accept answer from " + answer.getUsername());
                        return;
                }
                if (quiz.getQuestion(curRound.get()).isIndividual()) {
                        answers.put(answer, latch.countdown());
                } else {
                        ModifiedCyclicBarrier barrier = teamBarriers.get(answer.getTeamId());
                        if (barrier == null) {
                                throw new RuntimeException();
                        }
                        try {
                                answers.put(answer, barrier.await());
                        } catch (InterruptedException e) {
                        }
                        System.out.println("Counted down");
                        latch.countdown();
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

        private boolean isValidAnswer(Answer answer) {
                return started && latch != null && answer.getRoundNumber() == curRound.get()
                                && teamManager.hasPlayer(answer.getUsername());
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
                                clientHandlers.size());
        }

        private void scoreAnswers() {
                Question question = quiz.getQuestion(curRound.get());
                int correctOption = question.getCorrect();
                int points = question.getPoints();
                for (Map.Entry<Answer, Integer> entry : answers.entrySet()) {
                        Answer answer = entry.getKey();
                        int bonusFactor = entry.getValue();
                        Player player = teamManager.getPlayer(answer.getUsername());
                        if (player == null || bonusFactor == 0 || correctOption != answer.getOption())
                                continue;
                        int updatedScore = player.getScore() + (points * bonusFactor);
                        player.setScore(updatedScore);
                }
                answers.clear();
        }

}
