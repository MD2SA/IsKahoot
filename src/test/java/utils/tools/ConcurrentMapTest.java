package utils.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class ConcurrentMapTest {

    @Test
    void supportsBasicMapOperations() {
        ConcurrentMap<String, Integer> map = new ConcurrentMap<>();
        assertTrue(map.isEmpty());
        assertNull(map.put("a", 1));
        assertEquals(1, map.put("a", 2));

        assertEquals(2, map.get("a"));
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsValue(2));
        assertEquals(1, map.size());

        assertEquals(2, map.remove("a"));
        assertNull(map.get("a"));
        assertTrue(map.isEmpty());
    }

    @Test
    void putAllAndClearUpdateTheWholeMap() {
        ConcurrentMap<String, Integer> map = new ConcurrentMap<>();
        map.putAll(Map.of("a", 1, "b", 2));

        assertEquals(2, map.size());
        assertEquals(2, map.get("b"));

        map.clear();

        assertTrue(map.isEmpty());
    }

    @Test
    void exposesViewsOfTheCurrentContents() {
        ConcurrentMap<String, Integer> map = new ConcurrentMap<>();
        map.put("a", 1);
        map.put("b", 2);

        assertEquals(Set.of("a", "b"), map.keySet());
        assertEquals(List.of(1, 2), map.values().stream().sorted().toList());
        assertEquals(Set.of(Map.entry("a", 1), Map.entry("b", 2)), map.entrySet());
    }

    @Test
    @Timeout(5)
    void acceptsConcurrentPutsFromSeveralThreads() throws Exception {
        ConcurrentMap<Integer, Integer> map = new ConcurrentMap<>();
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
                        map.put(base + entry, base + entry);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }

        start.countDown();

        assertTrue(done.await(3, TimeUnit.SECONDS));
        assertEquals(threadCount * entriesPerThread, map.size());
    }
}
