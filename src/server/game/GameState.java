package server.game;

import server.Server;
import server.game.utils.Player;
import utils.quiz.Question;
import utils.quiz.Quiz;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class GameState {

        private final String id;
        private final Server server;

        private final TeamManager teamManager;
        private final Quiz quiz;
        private QuizManager quizManager;
        private Set<String> answeredPlayers = new HashSet<>();

        private boolean hasStarted;
        private final Object startLock = new Object();

        public GameState(String id, int teamCapacity, int numberOfTeams, Quiz quiz) {
                // this will never be null if only called by server
                this.server = Objects.requireNonNull(Server.getInstance());
                this.id = id;
                this.teamManager = new TeamManager(numberOfTeams, teamCapacity);
                this.quiz = quiz;
        }

        public boolean registerPlayer(String username, String teamName) {
                if (hasStarted)
                        return false;
                boolean ok = teamManager.registerPlayer(username, teamName);
                if (ok && teamManager.isFull()) {
                        Server.getInstance().getLogger().debug("GameState registerPlayer: Starting game");
                        start();
                }
                return ok;
        }

        public void answer(String username, int option) {
                // hasn't started or does have already answered this question
                if (!hasStarted || answeredPlayers.contains(username))
                        return;

                Player player = teamManager.getPlayer(username);
                if (player == null)
                        return;

                quizManager.submitAnswer(player, option);
                answeredPlayers.add(username);
        }

        private void start() throws IllegalStateException {
                synchronized (startLock) {
                        if (hasStarted) {
                                Server.getInstance().getLogger().warn("GameState start: game has already started");
                                throw new IllegalStateException("Game has already started");
                        }
                        this.quizManager = new QuizManager(quiz);
                        hasStarted = true;
                }

                while (quiz.hasNextQuestion()) {
                        Question question = quizManager.nextQuestion();
                        server.serveNextQuestion(id, question);
                        quizManager.awaitFinish();
                        server.serveScoreboard(id, teamManager.getScoreboard());
                        answeredPlayers.clear(); // reset players who have answered
                }

        }

        public String getCode() {
                return id;
        }

}
