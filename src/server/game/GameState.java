package server.game;

import server.Server;
import server.game.utils.ModifiedCountdownLatch;
import server.game.utils.Player;
import server.game.utils.Team;
import utils.Question;
import utils.Quiz;

import java.util.*;

public class GameState {

        private final int id;
        private final Server server;

        // Maps for o(1) search
        // userId, playerObj
        private final Map<String, Player> playerMap = new HashMap<>();
        //teamId, teamObj
        private final Map<String, Team> teamMap = new HashMap<>();

        private ModifiedCountdownLatch latch;

        private final int numberOfTeams;
        private final int teamCapacity;

        private Quiz quiz;
        private boolean hasStarted;

        public GameState(Server server, int id, int teamCapacity, int numberOfTeams) {
                this.server = server;
                this.id = id;
                this.teamCapacity = teamCapacity;
                this.numberOfTeams = numberOfTeams;
        }

        public synchronized boolean registerPlayer(String username, String teamName) {
                if(hasStarted) return false; // no registrations during game

                if (playerMap.containsKey(username)) // player is already registered
                        return false;

                if( !teamMap.containsKey(teamName) )  {
                        if( numberOfTeams <= teamMap.size() ) {
                                return false;
                        } else {
                                teamMap.put(teamName, new Team(teamName,teamCapacity));
                        }
                }
                Player player = new Player();

                if(!teamMap.get(teamName).addPlayer(player)) // failed to add to team
                        return false;

                playerMap.put(username, player);
                return true;
        }

        public synchronized void answer(String username, int option) {
                System.out.println("Player "+username+" is trying to answer");
                if( !hasStarted || !playerMap.containsKey(username) || latch == null )
                        return;

                System.out.println("Valid for classification! Reviewing answer...");
                int bonusFactor = latch.countdown();
                Question question = quiz.getCurQuestion();

                if(quiz.getCurQuestion().isCorrect(option)) {
                        Player player = playerMap.get(username);
                        int updatedScore = player.getScore() + question.getPoints()*bonusFactor;
                        player.setScore(updatedScore);
                }

        }

        public void start(Quiz quiz) throws IllegalStateException {
                synchronized (this) {
                        if( hasStarted )
                                throw new IllegalStateException("Game has already started");
                        hasStarted = true;
                }

                this.quiz = Objects.requireNonNull(quiz, "Invalid quiz provided");

                while (quiz.hasNextQuestion()) {
                        nextQuestion();

                        server.sendTeamScores(id, teamMap.values());
                        System.out.println("Next question");
                }
                System.out.println("No more Questions!");
        }

        private void nextQuestion() {
                Question question = quiz.nextQuestion();
                server.serveNextQuestion(id, question);

                this.latch = new ModifiedCountdownLatch(2,2,5,playerMap.size());
                latch.startTimer();
        }

}
