package utils.tools;

import java.util.List;

public class BlockingQueue<T> {

        private Node head;
        private Node tail;
        private int size = 0;

        public synchronized void put(T val) {
                Node node = new Node(val, null);
                if (tail == null) {
                        head = tail = node;
                } else {
                        tail.next = node;
                        tail = node;
                }
                size++;
                notifyAll();
        }

        public synchronized T take() throws InterruptedException {
                while (head == null) {
                        wait();
                }
                T val = head.val;
                head = head.next;
                if (head == null)
                        tail = null;
                size--;
                return val;
        }

        public synchronized void drainTo(List<T> list) {
                while (head != null) {
                        list.add(head.val);
                        head = head.next;
                }
                tail = null;
                size = 0;
        }

        public synchronized boolean isEmpty() {
                return size == 0;
        }

        public synchronized int size() {
                return size;
        }

        private class Node {
                T val;
                Node next;
                public Node(T val, Node next) {
                        this.val = val;
                        this.next = next;
                }
        }
}
