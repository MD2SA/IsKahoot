package server.game;

import java.util.List;
import java.util.Map;

import configs.Config;
import server.game.utils.Answer;
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

        public void prepareRound(int round, boolean isIndividual, int totalPlayers, List<Team> teams) {
                this.curRound = round;
                this.globalLatch = new ModifiedCountdownLatch(Config.Features.BONUS_COUNT,
                                Config.Features.BONUS_FACTOR,
                                Config.Features.ROUND_SECONDS,
                                totalPlayers);

                if (!(this.isIndividual = isIndividual)) {
                        barriers = new ConcurrentMap<>();
                        for (Team team : teams) {
                                barriers.put(team.getName(), new ModifiedCyclicBarrier(team.size()));
                        }
                }
                roundReady = true;
        }

        public void notifyAnswer(Answer answer) {
                if (answer.getRoundNumber() != curRound)
                        return;

                if (isIndividual) {
                        answer.setBonus(globalLatch.countdown());
                        return;
                }

                try {
                        int bonus = barriers.get(answer.getTeamId()).await();
                        System.out.println("stopped waiting on team");
                        answer.setBonus(bonus);
                } catch (InterruptedException e) {
                }
                globalLatch.countdown();
        }

        public void waitForRoundEnd() {
                try {
                        globalLatch.await();
                        if (!isIndividual) {
                                System.out.println("libertando");
                                barriers.values().forEach(b -> b.forceRelease());
                        }
                        System.out.println("end of round");
                } catch (InterruptedException e) {
                }
                roundReady = false;
        }

        public boolean isRoundReady() {
                return roundReady;
        }

}
