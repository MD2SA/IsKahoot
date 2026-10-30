package utils.tools;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ConcurrentSetTest {

    @Test
    void removeIfRemovesMatchingElements() {
        ConcurrentSet<String> set = new ConcurrentSet<>();
        set.add("alice");
        set.add("bob");
        set.add("carol");

        assertTrue(set.removeIf(name -> name.startsWith("a")));

        assertEquals(2, set.size());
        assertFalse(set.contains("alice"));
        assertTrue(set.contains("bob"));
        assertTrue(set.contains("carol"));
    }

    @Test
    void removeIfReturnsFalseWhenNothingMatches() {
        ConcurrentSet<String> set = new ConcurrentSet<>();
        set.add("alice");
        set.add("bob");

        assertFalse(set.removeIf(name -> name.startsWith("z")));

        assertEquals(2, set.size());
    }

    @Test
    void iteratorIsASnapshot() {
        ConcurrentSet<String> set = new ConcurrentSet<>();
        set.add("alice");

        assertDoesNotThrow(() -> {
            for (String name : set)
                set.add("bob");
        });

        assertEquals(2, set.size());
    }
}
