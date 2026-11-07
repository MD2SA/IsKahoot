package utils.quiz;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class RoundResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<String, Integer> results = new TreeMap<>();

    public void increaseScore(int score, String teamId) {
        results.put(teamId, results.getOrDefault(teamId, 0) + score);
    }

    public Map<String, Integer> getResult() {
        return Map.copyOf(this.results);
    }
}
