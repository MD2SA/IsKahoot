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
                this.curRound = round;
                this.globalLatch = new ModifiedCountdownLatch(Config.Features.BONUS_COUNT,
                                Config.Features.BONUS_FACTOR,
                                Config.Features.ROUND_SECONDS,
                                totalPlayers);
                gameState.resetAnswers();

                if (!(this.isIndividual = isIndividual)) {
                        barriers = new ConcurrentMap<>();
                        for (Team team : gameState.getTeams()) {
                                barriers.put(team.getName(), new ModifiedCyclicBarrier(team.size()));
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
                        int bonus = barriers.get(answer.getTeamId()).await();
                        answer.setBonus(bonus);
                } catch (InterruptedException e) {
                }
                globalLatch.countdown();
        }

        public void waitForRoundEnd() {
                try {
                        globalLatch.await();
                        if (!isIndividual) {
                                System.out.println("libertando equipas");
                                barriers.values().forEach(b -> b.forceRelease());
                        }
                } catch (InterruptedException e) {
                }
                roundReady = false;
                System.out.println("end of round");
        }

        public boolean isRoundReady() {
                return roundReady;
        }

}
