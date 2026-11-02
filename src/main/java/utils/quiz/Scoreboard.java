
package utils.quiz;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class Scoreboard implements Serializable {

    private final Map<String, Integer> totals = new TreeMap<>();

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

    public Map<String, Integer> getScoreboard() {
        return Map.copyOf(totals);
    }

}
