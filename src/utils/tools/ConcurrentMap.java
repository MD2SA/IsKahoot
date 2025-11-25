package utils.tools;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ConcurrentMap<K, V> {

        private final Map<K, V> map = new HashMap<>();

        public synchronized boolean containsKey(K key) {
                return map.containsKey(key);
        }

        public synchronized V put(K key, V value) {
                return map.put(key, value);
        }

        public synchronized V get(K key) {
                return map.get(key);
        }

        public V getOrDefault(K key, V defaultValue) {
                return map.getOrDefault(key, defaultValue);
        }

        public synchronized void clear() {
                map.clear();
        }

        public synchronized int size() {
                return map.size();
        }

        public synchronized Collection<V> values() {
                return map.values();
        }

        public synchronized V remove(K key) {
                return map.remove(key);
        }


}
