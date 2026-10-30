package utils.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.junit.jupiter.api.Test;

import server.game.utils.Player;
import server.game.utils.Team;
import utils.quiz.Question;
import utils.quiz.Quiz;
import utils.quiz.QuizCreator;
import utils.quiz.ReducedQuestion;

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
    void roundTripsTheUpdateMessage() throws Exception {
        Team team = new Team("A", 2);
        team.addPlayer(new Player("alice"));
        team.increaseScore(15);

        MessageFromServer message = roundTrip(
                MessageFromServer.updateMessage(new ReducedQuestion(loadQuestion(), 0), List.of(team)));

        assertEquals(Code.UPDATE, message.getCode());
        assertEquals(0, message.getRoundNumber());
        assertEquals("Which keyword starts a thread?", message.getQuestion().getQuestion());
        assertEquals(5, message.getQuestion().getPoints());
        assertEquals(4, message.getQuestion().getOptions().size());
        assertEquals("A", message.getScoreboard().get(0).getName());
        assertEquals(15, message.getScoreboard().get(0).getScore());
    }

    @Test
    void roundTripsTheEndMessage() throws Exception {
        Team team = new Team("A", 2);
        team.increaseScore(42);

        MessageFromServer message = roundTrip(MessageFromServer.endMessage(List.of(team)));

        assertEquals(Code.END, message.getCode());
        assertNull(message.getQuestion());
        assertEquals(-1, message.getRoundNumber());
        assertEquals("A", message.getScoreboard().get(0).getName());
        assertEquals(42, message.getScoreboard().get(0).getScore());
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

    private static Question loadQuestion() throws IOException {
        try (InputStream input = ProtocolSerializationTest.class.getClassLoader()
                .getResourceAsStream("quiz-fast.json")) {
            assertNotNull(input, "quiz-fast.json is missing");
            Quiz quiz = QuizCreator.getFrom(input).get(0);
            return quiz.getQuestion(0);
        }
    }
}
