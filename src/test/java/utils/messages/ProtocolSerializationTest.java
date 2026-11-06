package utils.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.Test;

import utils.quiz.Quiz;
import utils.quiz.QuizCreator;
import utils.quiz.ReducedQuestion;
import utils.quiz.Score;
import utils.quiz.Scoreboard;

class ProtocolSerializationTest {

    @Test
    void roundTripsTheRegistrationMessage() throws Exception {
        MessageFromClient message = roundTrip(MessageFromClient.getRegistrationMessage("alice", "A", "ABCD"));

        assertEquals(Code.REGISTER, message.getCode());
        assertEquals("alice", message.getClientId());
        assertEquals("A", message.getTeamId());
        assertEquals("ABCD", message.getGameId());
        assertEquals(-1, message.getAnswer().getRoundNumber());
    }

    @Test
    void roundTripsTheAnswerMessage() throws Exception {
        MessageFromClient message = roundTrip(MessageFromClient.getAnswerQuestion("alice", "A", 2, 3));

        assertEquals(Code.ANSWER, message.getCode());
        assertEquals("alice", message.getAnswer().getUsername());
        assertEquals("A", message.getAnswer().getTeamId());
        assertEquals(2, message.getAnswer().getOption());
        assertEquals(3, message.getAnswer().getRoundNumber());
    }

    @Test
    void roundTripsQuestionDeadline() throws Exception {
        ReducedQuestion question = new ReducedQuestion(loadQuiz().getQuestion(0), 0, 123_456L);
        MessageFromServer message = roundTrip(MessageFromServer.questionMessage(question));

        assertEquals(Code.QUESTION, message.getCode());
        assertEquals(123_456L, message.getQuestion().getDeadlineEpochMillis());
    }

    @Test
    void roundTripsScoreboardMessage() throws Exception {
        Scoreboard scoreboard = new Scoreboard(List.of("A"));
        MessageFromServer message = roundTrip(MessageFromServer.scoreboardMessage(scoreboard));

        assertEquals(Code.SCOREBOARD, message.getCode());
        assertEquals(List.of(new Score("A", 0)), message.getScoreboard());
    }

    private static Quiz loadQuiz() throws IOException {
        try (InputStream input = ProtocolSerializationTest.class.getClassLoader()
                .getResourceAsStream("quiz-fast.json")) {
            return QuizCreator.getFrom(input).get(0);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T roundTrip(T value) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(value);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (T) in.readObject();
        }
    }

}
