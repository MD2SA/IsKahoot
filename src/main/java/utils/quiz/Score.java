
package utils.quiz;

import java.io.Serializable;

public record Score(String teamId, int points) implements Serializable {
}
