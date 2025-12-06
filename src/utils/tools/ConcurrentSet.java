package utils.tools;

import java.util.ArrayList;
import java.util.List;

public class ConcurrentSet<T> {

        private final List<T> elements;

        public ConcurrentSet() {
                this.elements = new ArrayList<>();
        }

        public synchronized boolean add(T element) {
                if (!elements.contains(element)) {
                        elements.add(element);
                        return true;
                }
                return false;
        }

        public synchronized boolean remove(T element) {
                return elements.remove(element);
        }

        public synchronized boolean contains(T element) {
                return elements.contains(element);
        }

        public synchronized int size() {
                return elements.size();
        }

}
