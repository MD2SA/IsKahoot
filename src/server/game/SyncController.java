package server.game;

import java.util.Map;

import configs.Config;
import utils.quiz.Answer;
import server.game.utils.ModifiedCountdownLatch;
import server.game.utils.ModifiedCyclicBarrier;
import server.game.utils.Team;
import utils.tools.ConcurrentMap;

public class SyncController {

        private ModifiedCountdownLatch globalLatch;
        private Map<String, ModifiedCyclicBarrier> barriers;

        private boolean isIndividual;
        private int curRound;

        private boolean roundReady;
        private boolean closing;

        private int activeAnswers;

        synchronized void prepareRound(int round, boolean isIndividual, int totalPlayers, GameState gameState) {
                gameState.resetAnswers();
                // if isIndividual use bonus of countdown
                // else just use globalLatch has normal countdownlatch with timer
                this.globalLatch = new ModifiedCountdownLatch(Config.Features.BONUS_COUNT,
                                Config.Features.BONUS_FACTOR,
                                Config.Features.ROUND_SECONDS,
                                totalPlayers);

                if (!(this.isIndividual = isIndividual)) {
                        barriers = new ConcurrentMap<>();
                        for (Team team : gameState.getTeams()) {
                                barriers.put(team.getName(), new ModifiedCyclicBarrier(team.size()));
                        }
                }
                this.curRound = round;
                this.activeAnswers = 0;
                this.closing = false;
                this.roundReady = true;
        }

        void notifyAnswer(Answer answer) {
                synchronized (this) {
                        if (!roundReady || answer.getRoundNumber() != curRound)
                                return;
                        activeAnswers++;
                }

                try {
                        int individualBonus = globalLatch.countdown();
                        if (isIndividual) {
                                answer.setBonus(individualBonus);
                        } else {
                                ModifiedCyclicBarrier barrier = barriers.get(answer.getTeamId());
                                if (barrier != null) {
                                        answer.setBonus(barrier.await());
                                }
                        }
                } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                } finally {
                        synchronized (this) {
                                activeAnswers--;
                                if (closing && activeAnswers == 0) {
                                        System.out.println("notified all");
                                        notifyAll();
                                }
                        }
                }
        }

        void waitForRoundEnd() throws InterruptedException {
                globalLatch.await();

                synchronized (this) {
                        roundReady = false;
                        closing = true;
                        System.out.println("closing");
                }

                if (!isIndividual)
                        barriers.values().forEach(b -> b.forceRelease());

                synchronized (this) {
                        while (activeAnswers > 0)
                                wait();
                        System.out.println("finished closing");
                        closing = false;
                }
        }

        public boolean isRoundReady() {
                return roundReady;
        }

}
