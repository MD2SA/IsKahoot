package server.game;

import server.Server;
import server.game.utils.ModifiedCountdownLatch;
import server.game.utils.Player;
import utils.quiz.Question;
import utils.quiz.Quiz;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import configs.Config;

public class GameState extends Thread {

        private final String id;
        private final Server server;

        private ModifiedCountdownLatch latch;
        private final TeamManager teamManager;
        private final Quiz quiz;
        private Set<String> answeredPlayers = new HashSet<>();

        private boolean hasStarted;
        private final Object startLock = new Object();

        public GameState(String id, int teamCapacity, int numberOfTeams, Quiz quiz) {
                // this will never be null if only called by server
                this.server = Objects.requireNonNull(Server.getInstance());
                this.id = id;
                this.teamManager = new TeamManager(numberOfTeams, teamCapacity);
                this.quiz = quiz;
                this.latch = new ModifiedCountdownLatch(Config.Features.BONUS_COUNT, Config.Features.BONUS_FACTOR,
                                Config.Features.ROUND_SECONDS, teamManager.getCapacity());
        }

        public boolean registerPlayer(String username, String teamName) {
                if (hasStarted)
                        return false;
                return teamManager.registerPlayer(username, teamName);
        }

        public void answer(String username, int option) {
                // hasn't started or does have already answered this question
                if (!hasStarted || answeredPlayers.contains(username))
                        return;
                server.getLogger().debug("Handling answer of " + username);

                Player player = teamManager.getPlayer(username);
                if (player == null)
                        return;

                submitAnswer(player, option);
                answeredPlayers.add(username);
        }

        @Override
        public void run() {
                synchronized (startLock) {
                        if (hasStarted) {
                                Server.getInstance().getLogger().warn("GameState start: game has already started");
                                throw new IllegalStateException("Game has already started");
                        }
                        hasStarted = true;
                        Server.getInstance().getLogger().debug("GameState registerPlayer: Starting game");
                }

                while (quiz.hasNextQuestion()) {
                        Question question = quiz.nextQuestion();
                        server.serveNextQuestion(id, question);
                        try {
                                server.getLogger().debug("GAME AWAITING ID:" + id);
                                latch.await();
                        } catch (InterruptedException e) {
                                Server.getInstance().getLogger().error("GameState start latch: unwanted interrupted");
                        }
                        server.serveScoreboard(id, teamManager.getScoreboard());
                        answeredPlayers.clear(); // reset players who have answered
                }
                server.killGame(this.id);
        }

        public String getCode() {
                return id;
        }

        public boolean isFull() {
                return teamManager.isFull();
        }

        public boolean hasStarted() {
                return hasStarted;
        }

        public void submitAnswer(Player player, int option) {
                if (quiz == null || latch == null)
                        throw new IllegalStateException();

                Question question = quiz.getCurQuestion();

                int bonusFactor = latch.countdown();

                if (!question.isCorrect(option))
                        return;

                int updatedScore = player.getScore() + question.getPoints() * bonusFactor;
                player.setScore(updatedScore);
        }

}
