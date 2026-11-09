package utils.tools;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class ConcurrentSetTest {

    @Test
    void supportsTheBasicSetOperations() {
        ConcurrentSet<String> set = new ConcurrentSet<>();

        assertTrue(set.isEmpty());
        assertTrue(set.add("alice"));
        assertFalse(set.add("alice"));
        assertTrue(set.add("bob"));

        assertEquals(2, set.size());
        assertTrue(set.contains("alice"));
        assertFalse(set.contains("carol"));

        assertTrue(set.remove("alice"));
        assertFalse(set.remove("alice"));
        assertEquals(Set.of("bob"), Set.copyOf(set));

        set.clear();

        assertTrue(set.isEmpty());
        assertEquals(0, set.size());
    }

    @Test
    void supportsBulkOperations() {
        ConcurrentSet<String> set = new ConcurrentSet<>();
        assertTrue(set.addAll(List.of("alice", "bob", "carol")));

        assertTrue(set.containsAll(List.of("alice", "carol")));
        assertFalse(set.containsAll(List.of("alice", "dave")));

        assertTrue(set.retainAll(List.of("alice", "bob")));
        assertEquals(Set.of("alice", "bob"), Set.copyOf(set));

        assertTrue(set.removeAll(List.of("alice")));
        assertEquals(Set.of("bob"), Set.copyOf(set));
    }

    @Test
    void exposesArraysAndStreamsOfTheCurrentContents() {
        ConcurrentSet<String> set = new ConcurrentSet<>();
        set.addAll(List.of("alice", "bob"));

        Object[] objects = set.toArray();
        String[] strings = set.toArray(new String[0]);

        assertEquals(2, objects.length);
        Arrays.sort(objects);
        Arrays.sort(strings);
        assertArrayEquals(new String[]{"alice", "bob"}, objects);
        assertArrayEquals(new String[]{"alice", "bob"}, strings);
        assertEquals(List.of("alice", "bob"), set.stream().sorted().toList());
    }

    @Test
    void iteratorIsASnapshot() {
        ConcurrentSet<String> set = new ConcurrentSet<>();
        set.add("alice");

        assertDoesNotThrow(() -> {
            for (String name : set) {
                assertEquals("alice", name);
                set.add("bob");
            }
        });

        assertEquals(2, set.size());
    }

    @Test
    @Timeout(5)
    void acceptsConcurrentAddsFromSeveralThreads() throws Exception {
        ConcurrentSet<Integer> set = new ConcurrentSet<>();
        int threadCount = 4;
        int entriesPerThread = 100;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        for (int thread = 0; thread < threadCount; thread++) {
            int base = thread * entriesPerThread;
            new Thread(() -> {
                try {
                    start.await();
                    for (int entry = 0; entry < entriesPerThread; entry++)
                        set.add(base + entry);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }

        start.countDown();

        assertTrue(done.await(3, TimeUnit.SECONDS));
        assertEquals(threadCount * entriesPerThread, set.size());
    }
}
