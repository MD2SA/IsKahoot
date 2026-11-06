package server.game.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

class TeamSerializationTest {

    @Test
    void roundTripsTeamPlayers() throws Exception {
        Team team = new Team("A", 2);
        team.addPlayer(new Player("alice"));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(team);
        }

        Team restored;
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (Team) input.readObject();
        }

        assertEquals(1, restored.size());
        assertEquals("alice", restored.getPlayers().get(0).getName());
    }
}
