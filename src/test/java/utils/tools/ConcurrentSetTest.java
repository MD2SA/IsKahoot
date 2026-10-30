package utils.tools;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ConcurrentSetTest {

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
