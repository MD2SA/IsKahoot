
package utils.quiz;

import java.io.Serializable;

public record Score(String teamId, int points) implements Comparable<Score>, Serializable {

    @Override
    public int compareTo(Score other) {
        int pointsOrder = Integer.compare(other.points, this.points);
        return pointsOrder != 0 ? pointsOrder : this.teamId.compareTo(other.teamId);
    }
}
