package server.game;

import server.Server;
import server.game.utils.Player;
import utils.quiz.Question;
import utils.quiz.Quiz;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GameState {

        private final int id;
        private final Server server;

        private final TeamManager teamManager;
        private QuizManager quizManager;
        private Set<String> answeredPlayers = new HashSet<>();

        private boolean hasStarted;
        private final Object startLock = new Object();

        public GameState(int id, int teamCapacity, int numberOfTeams) throws IOException {
                this.server = Server.getInstance();
                this.id = id;
                this.teamManager = new TeamManager(numberOfTeams,teamCapacity);
        }

        public boolean registerPlayer(String username, String teamName) {
                if(hasStarted) return false;
                return teamManager.registerPlayer(username, teamName);
        }

        public void answer(String username, int option) {
                // hasn't started or does have already answered this question
                if(!hasStarted || answeredPlayers.contains(username)) return;

                Player player = teamManager.getPlayer(username);
                if(player == null) return;

                quizManager.submitAnswer(player, option);
                answeredPlayers.add(username);
        }

        public void start(Quiz quiz) throws IllegalStateException {
                synchronized(startLock) {
                        if(hasStarted) throw new IllegalStateException("Game has already started");
                        this.quizManager = new QuizManager(quiz, teamManager.isMultiPlayer());
                        hasStarted = true;
                }

                while (quiz.hasNextQuestion()) {
                        Question question = quizManager.nextQuestion();
                        server.serveNextQuestion(id, question);
                        quizManager.awaitFinish();
                        server.sendTeamScores(id, teamManager.getScoreboard());
                        answeredPlayers.clear(); //reset players who have answered
                }

        }

}
