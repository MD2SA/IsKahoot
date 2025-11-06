package server;

import utils.Question;
import utils.Quiz;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GameState {

        private final int id;
        private final Server server;

        // teamId, score
        private final Map<Integer, Integer> scores = new HashMap<>();
        // userId, teamId
        private final Map<Integer, Integer> playerMap = new HashMap<>();

        private int roundAnswers = 0;

        private final int numberOfTeams;

        private Quiz quiz;
        private boolean hasStarted;

        public GameState(Server server, int id, int numberOfTeams) {
                this.server = server;
                this.id = id;
                this.numberOfTeams = numberOfTeams;
        }

        public synchronized boolean registerPlayer(int playerId, int teamId) {
                if( !scores.containsKey(teamId) )  {
                        if( scores.size() >= numberOfTeams ) {
                                return false;
                        } else {
                                scores.put(teamId, 0);
                        }
                }

                playerMap.put(playerId, teamId);
                return true;
        }

        public synchronized void answer(int playerId, int option) {
                System.out.println("Player "+playerId+" is Answering");
                if( !hasStarted || !playerMap.containsKey(playerId) )
                        return;

                Question question = quiz.getCurQuestion();
                roundAnswers++;

                System.out.println("Player "+playerId+" answered");
                if(quiz.getCurQuestion().isCorrect(option)) {
                        int teamId = playerMap.get(playerId);
                        scores.put(teamId, scores.get(teamId)+question.getPoints());
                        System.out.println("Team "+teamId+" now has "+ scores.get(teamId));
                }

        }

        public void start(Quiz quiz) throws InterruptedException, IllegalStateException {
                synchronized (this) {
                        if( hasStarted )
                                throw new IllegalStateException("Game has already started");
                        hasStarted = true;
                }

                this.quiz = Objects.requireNonNull(quiz, "Invalid quiz provided");

                while (quiz.hasNextQuestion()) {
                        nextQuestion();
                        roundAnswers = 0;

                        Thread thread = new Thread(() -> {
                                try {
                                        int secondsLeft = 10;
                                        while (secondsLeft > 0 && !allAnswersSubmitted()) {
                                                Thread.sleep(1000);
                                                secondsLeft--;
                                        }
                                } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                }
                        });

                        thread.start();
                        thread.join();

                        server.sendTeamScores(id, scores);
                        System.out.println("Next utils.Question");
                }

                System.out.println("No more Questions!");
        }

        public boolean allAnswersSubmitted() {
                return roundAnswers>=playerMap.size();
        }

        private void nextQuestion() {
                quiz.nextQuestion();
                server.notifyFromGame(id);
        }

}
