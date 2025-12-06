package utils.tools;

public class ConcurrentPair<K, V> {
        private K key;
        private V value;

        public ConcurrentPair() {
                this.key = null;
                this.value = null;
        }

        public ConcurrentPair(K key, V value) {
                this.key = key;
                this.value = value;
        }

        public synchronized void put(K key, V value) {
                this.key = key;
                this.value = value;
        }

        public synchronized V get(K key) {
                if (this.key == null)
                        return null;
                if (this.key.equals(key)) {
                        return this.value;
                }
                return null;
        }

        public synchronized void remove(K key) {
                if (this.key != null && this.key.equals(key)) {
                        this.key = null;
                        this.value = null;
                }
        }

        public synchronized boolean containsKey(K key) {
                return this.key != null && this.key.equals(key);
        }

}
