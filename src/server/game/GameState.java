package server.game;

import server.Server;
import utils.quiz.Answer;
import server.game.utils.Player;
import utils.quiz.Quiz;
import utils.quiz.Score;
import server.game.utils.Team;
import utils.tools.ConcurrentMap;

import java.util.ArrayList;
import java.util.List;

public class GameState {

        private final Quiz quiz;

        private final ConcurrentMap<String, Player> playerMap;
        private final ConcurrentMap<String, Team> teamMap;

        private final int numberOfTeams;
        private final int teamCapacity;

        private final ConcurrentMap<Player, Answer> answers = new ConcurrentMap<>();

        GameState(Quiz quiz, int numberOfTeams, int teamCapacity) {
                this.quiz = quiz;
                this.playerMap = new ConcurrentMap<>();
                this.numberOfTeams = numberOfTeams;
                this.teamCapacity = teamCapacity;
                this.teamMap = isSinglePlayer() ? null : new ConcurrentMap<>();
        }

        synchronized boolean registerPlayer(String username, String teamName) {
                // player is already registered
                if (playerMap.containsKey(username)) {
                        Server.getInstance().getLogger()
                                        .debug("TeamManager registerPlayer: " + username + " is already registered");
                        return false;
                }

                if (!isSinglePlayer() && !teamMap.containsKey(teamName)) {
                        // max teams registered
                        if (numberOfTeams <= teamMap.size()) {
                                Server.getInstance().getLogger().debug("TeamManager registerPlayer: max teams reached");
                                return false;
                        }
                        teamMap.put(teamName, new Team(teamName, teamCapacity));
                }

                Player player = new Player(username);
                // failed to add to team
                if (!isSinglePlayer() && !teamMap.get(teamName).addPlayer(player)) {
                        Server.getInstance().getLogger().debug("TeamManager registerPlayer: failed to add to team");
                        return false;
                }

                playerMap.put(username, player);
                return true;
        }

        boolean remove(String username) {
                Player player = playerMap.get(username);
                if (player == null)
                        return true;
                // TODO: remove from team
                return true;
        }

        void answer(Answer answer, SyncController sync) {
                if (!isValidAnswer(answer)) {
                        Server.getInstance().getLogger().info("Cannot accept answer from " + answer.getUsername());
                        return;
                }
                answers.put(playerMap.get(answer.getUsername()), answer);
                sync.notifyAnswer(answer);
        }

        boolean isFull() {
                return teamCapacity * numberOfTeams <= playerMap.size();
        }

        private boolean isValidAnswer(Answer answer) {
                Player player = playerMap.get(answer.getUsername());
                if (player == null)
                        return false;
                return !answers.containsKey(player);
        }

        List<Team> getTeams() {
                return new ArrayList<>(teamMap.values());
        }

        List<Score> getScoreboard() {
                if (isSinglePlayer())
                        return new ArrayList<>(playerMap.values());
                else
                        return new ArrayList<>(teamMap.values());
        }

        ConcurrentMap<Player, Answer> getAnswers() {
                return answers;
        }

        Quiz getQuiz() {
                return quiz;
        }

        private boolean isSinglePlayer() {
                return teamCapacity == 1;
        }

        void resetAnswers() {
                answers.clear();
        }

}
