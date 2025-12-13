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
        private boolean roundReady;
        private int curRound;

        void prepareRound(int round, boolean isIndividual, int totalPlayers, GameState gameState) {
                gameState.resetAnswers();

                int totalWaiters = isIndividual ? totalPlayers : gameState.getTeams().size();

                this.curRound = round;
                this.globalLatch = new ModifiedCountdownLatch(Config.Features.BONUS_COUNT,
                                Config.Features.BONUS_FACTOR,
                                Config.Features.ROUND_SECONDS,
                                totalWaiters);

                if (!(this.isIndividual = isIndividual)) {
                        barriers = new ConcurrentMap<>();
                        for (Team team : gameState.getTeams()) {
                                barriers.put(team.getName(),
                                                new ModifiedCyclicBarrier(team.size(), () -> globalLatch.countdown()));
                        }
                }
                roundReady = true;
        }

        void notifyAnswer(Answer answer) {
                if (answer.getRoundNumber() != curRound)
                        return;

                if (isIndividual) {
                        answer.setBonus(globalLatch.countdown());
                        return;
                }
                try {
                        ModifiedCyclicBarrier barrier = barriers.get(answer.getTeamId());
                        answer.setBonus(barrier.await());
                } catch (InterruptedException e) {
                }
        }

        void waitForRoundEnd() throws InterruptedException {
                globalLatch.await();
                if (!isIndividual)
                        barriers.values().forEach(b -> b.forceRelease());
                roundReady = false;
        }

        public boolean isRoundReady() {
                return roundReady;
        }

}
