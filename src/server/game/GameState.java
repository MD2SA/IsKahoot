package server.game;

import server.Server;
import server.game.utils.Player;
import utils.Question;
import utils.Quiz;

public class GameState {

        private final int id;
        private final Server server;

        private final TeamManager teamManager;
        private final QuizManager quizManager;

        private boolean hasStarted;
        private final Object startLock = new Object();

        public GameState(Server server, int id, int teamCapacity, int numberOfTeams) {
                this.server = server;
                this.id = id;
                this.teamManager = new TeamManager(numberOfTeams,teamCapacity);
                this.quizManager = new QuizManager();
        }

        public boolean registerPlayer(String username, String teamName) {
                synchronized (startLock) {
                        if(hasStarted) return false;
                        return teamManager.registerPlayer(username, teamName);
                }
        }

        public void answer(String username, int option) {
                if(!hasStarted) return;

                Player player = teamManager.getPlayer(username);
                if(player == null) return;

                quizManager.submitAnswer(player, option);
        }

        public void start(Quiz quiz) throws IllegalStateException {
                synchronized(startLock) {
                        if(hasStarted) throw new IllegalStateException("Game has already started");
                        hasStarted = true;
                }
                quizManager.startQuiz(quiz);

                while (quiz.hasNextQuestion()) {
                        Question question = quizManager.nextQuestion();
                        server.serveNextQuestion(id, question);
                        quizManager.latch().await();
                        server.sendTeamScores(id, teamManager.getTeams());
                }

        }

}
