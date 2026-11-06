
package utils.quiz;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scoreboard implements Serializable {

    private final Map<String, Integer> totals = new HashMap<>();

    public Scoreboard(Collection<String> roster) {
        roster.forEach(e -> totals.put(e, 0));
    }

    public void applyResult(RoundResult result) {
        if (result == null)
            return;

        var resultMap = result.getResult();
        for (var entry : resultMap.entrySet()) {
            String teamId = entry.getKey();
            int score = entry.getValue();

            totals.put(teamId, totals.getOrDefault(teamId, 0) + score);
        }
    }

    public List<Score> getScoreboard() {
        return totals.entrySet().stream().map(e -> new Score(e.getKey(), e.getValue())).sorted().toList();
    }

}
