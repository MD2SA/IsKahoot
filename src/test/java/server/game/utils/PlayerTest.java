package server.game.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PlayerTest {

    @Test
    void exposesThePlayerName() {
        assertEquals("alice", new Player("alice").getName());
    }

    @Test
    void considersPlayersEqualWhenTheirNamesMatch() {
        Player alice = new Player("alice");
        Player sameName = new Player("alice");
        Player bob = new Player("bob");

        assertEquals(alice, alice);
        assertEquals(alice, sameName);
        assertEquals(alice.hashCode(), sameName.hashCode());
        assertNotEquals(alice, bob);
        assertNotEquals(alice, "alice");
    }

    @Test
    void playersCanBeUsedInHashBasedCollections() {
        java.util.Set<Player> players = new java.util.HashSet<>();
        players.add(new Player("alice"));

        assertTrue(players.contains(new Player("alice")));
    }
}
