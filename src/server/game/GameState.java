package server.game;

import server.Server;
import server.game.utils.Answer;
import server.game.utils.Player;
import utils.quiz.Quiz;
import utils.quiz.Score;
import server.game.utils.Team;
import utils.tools.ConcurrentMap;

import java.util.List;

public class GameState {

        private final Quiz quiz;
        private final TeamManager teamManager;

        private final ConcurrentMap<Player, Answer> answers = new ConcurrentMap<>();

        public GameState(Quiz quiz, int numberOfTeams, int teamCapacity) {
                this.quiz = quiz;
                this.teamManager = new TeamManager(numberOfTeams, teamCapacity);
        }

        boolean registerPlayer(String username, String teamName) {
                if (!teamManager.registerPlayer(username, teamName))
                        return false;
                return true;
        }

        boolean remove(String username) {
                teamManager.remove(username);

                return true;
        }

        void answer(Answer answer, SyncController sync) {
                if (!isValidAnswer(answer)) {
                        Server.getInstance().getLogger().info("Cannot accept answer from " + answer.getUsername());
                        return;
                }
                answers.put(teamManager.getPlayer(answer.getUsername()), answer);
                sync.notifyAnswer(answer);
        }

        public boolean isFull() {
                return teamManager.isFull();
        }

        private boolean isValidAnswer(Answer answer) {
                return teamManager.hasPlayer(answer.getUsername());
                // && !answeredPlayers.contains(username);
        }

        List<Team> getTeams() {
                return teamManager.getTeams();
        }

        List<Score> getScoreboard() {
                return teamManager.getScoreboard();
        }

        ConcurrentMap<Player, Answer> getAnswers() {
                return answers;
        }

        Quiz getQuiz() {
                return quiz;
        }

}
