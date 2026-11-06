package utils.messages;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import utils.quiz.ReducedQuestion;
import utils.quiz.Score;

public sealed interface ServerMessage extends Serializable permits ServerMessage.Question, ServerMessage.Scoreboard,
        ServerMessage.AnswerAccepted, ServerMessage.AnswerRejected, ServerMessage.End, ServerMessage.Error {

    record Question(ReducedQuestion question) implements ServerMessage {
        public Question {
            Objects.requireNonNull(question, "question");
        }
    }

    record Scoreboard(List<Score> scores) implements ServerMessage {
        public Scoreboard {
            scores = List.copyOf(scores);
        }
    }

    record AnswerAccepted(int roundNumber) implements ServerMessage {
    }

    record AnswerRejected(int roundNumber, String reason) implements ServerMessage {
        public AnswerRejected {
            Objects.requireNonNull(reason, "reason");
        }
    }

    record End() implements ServerMessage {
    }

    record Error(String reason) implements ServerMessage {
        public Error {
            Objects.requireNonNull(reason, "reason");
        }
    }
}
