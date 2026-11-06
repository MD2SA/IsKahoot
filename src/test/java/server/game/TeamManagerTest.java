package server.game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TeamManagerTest {

    @Test
    void enforcesTeamCapacity() {
        TeamManager manager = new TeamManager(2, 2);

        assertTrue(manager.registerPlayer("alice", "A"));
        assertTrue(manager.registerPlayer("bob", "A"));
        assertFalse(manager.registerPlayer("carol", "A"), "Team A is already full");
        assertTrue(manager.registerPlayer("carol", "B"));
    }

    @Test
    void rejectsDuplicateUsernames() {
        TeamManager manager = new TeamManager(2, 2);

        assertTrue(manager.registerPlayer("alice", "A"));
        assertFalse(manager.registerPlayer("alice", "B"));
    }

    @Test
    void removingAPlayerFreesASeat() {
        TeamManager manager = new TeamManager(1, 1);

        assertTrue(manager.registerPlayer("alice", "A"));
        assertFalse(manager.registerPlayer("bob", "A"));

        manager.removePlayer("alice");

        assertTrue(manager.registerPlayer("bob", "A"));
    }

    @Test
    void reportsWhetherTheLobbyIsFull() {
        TeamManager manager = new TeamManager(1, 2);

        assertFalse(manager.isFull());
        manager.registerPlayer("alice", "A");
        assertFalse(manager.isFull());
        manager.registerPlayer("bob", "A");
        assertTrue(manager.isFull());
    }

    @Test
    void rejectsInvalidConfigurationAndBlankIdentity() {
        assertThrows(IllegalArgumentException.class, () -> new TeamManager(0, 2));
        assertThrows(IllegalArgumentException.class, () -> new TeamManager(2, 0));

        TeamManager manager = new TeamManager(1, 1);
        assertFalse(manager.registerPlayer(" ", "A"));
        assertFalse(manager.registerPlayer("alice", " "));
    }

    @Test
    void returnedRosterCollectionsCannotMutateManagerState() {
        TeamManager manager = new TeamManager(1, 1);
        assertTrue(manager.registerPlayer("alice", "A"));

        assertThrows(UnsupportedOperationException.class, () -> manager.getUsernames().clear());
        assertThrows(UnsupportedOperationException.class, () -> manager.getRosterSnapshot().clear());
    }
}
