package utils.tools;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import java.util.stream.Stream;


public class ConcurrentSet<T> implements Set<T> {

        private final Set<T> elements;

        public ConcurrentSet() {
                this.elements = new HashSet<>();
        }

        @Override
        public synchronized boolean add(T element) {
                return elements.add(element);
        }

        @Override
        public synchronized boolean remove(Object element) {
                return elements.remove(element);
        }

        @Override
        public synchronized boolean contains(Object element) {
                return elements.contains(element);
        }

        @Override
        public synchronized int size() {
                return elements.size();
        }

        @Override
        public synchronized boolean isEmpty() {
                return elements.isEmpty();
        }

        @Override
        public synchronized void clear() {
                elements.clear();
        }

        @Override
        public synchronized boolean containsAll(Collection<?> c) {
                return elements.containsAll(c);
        }

        @Override
        public synchronized boolean addAll(Collection<? extends T> c) {
                return elements.addAll(c);
        }

        @Override
        public synchronized boolean retainAll(Collection<?> c) {
                return elements.retainAll(c);
        }

        @Override
        public synchronized boolean removeAll(Collection<?> c) {
                return elements.removeAll(c);
        }

        @Override
        public synchronized Iterator<T> iterator() {
                return new HashSet<>(elements).iterator();
        }

        @Override
        public synchronized Object[] toArray() {
                return elements.toArray();
        }

        @Override
        public synchronized <K> K[] toArray(K[] a) {
                return elements.toArray(a);
        }

        public synchronized Stream<T> stream() {
                return elements.stream();
        }
}
