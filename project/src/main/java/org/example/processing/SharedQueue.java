package org.example.processing;

import java.util.LinkedList;
import java.util.Queue;

public class SharedQueue<T> {
    private static final int CAPACITY = 100;
    private final Queue<T> queue = new LinkedList<>();
    private volatile boolean active = true;

    public synchronized void produce(T value) throws InterruptedException {
        while (queue.size() == CAPACITY && active) {
            wait();
        }
        if (!active) {
            return;
        }
        queue.add(value);
        notifyAll();
    }

    public synchronized T consume() throws InterruptedException {
        while (queue.isEmpty()) {
            if (!active) {
                return null;
            }
            wait();
        }
        T value = queue.poll();
        notifyAll();
        return value;
    }

    public synchronized void shutdown() {
        active = false;
        notifyAll();
    }
}
