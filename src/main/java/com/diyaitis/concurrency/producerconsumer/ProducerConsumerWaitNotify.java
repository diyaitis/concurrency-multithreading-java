package com.diyaitis.concurrency.producerconsumer;

import java.util.LinkedList;
import java.util.Queue;

/**
 * The producer-consumer pattern, implemented from scratch with wait()/
 * notifyAll() to show what BlockingQueue does for you under the hood.
 *
 * wait() releases the lock and suspends the thread; notifyAll() wakes every
 * thread waiting on that same object's monitor. Two rules that are easy to
 * get wrong:
 *  1. wait()/notifyAll() must be called while holding the monitor lock
 *     (i.e. inside a synchronized block on the same object).
 *  2. Always re-check your condition in a `while` loop, not `if` - a
 *     spurious wakeup, or another thread grabbing the item/slot first
 *     after notifyAll() wakes everyone, means the condition might already
 *     be false again by the time you resume.
 */
public class ProducerConsumerWaitNotify {

    private final Queue<Integer> buffer = new LinkedList<>();
    private final int capacity;
    private final Object lock = new Object();

    public ProducerConsumerWaitNotify(int capacity) {
        this.capacity = capacity;
    }

    public void produce(int value) throws InterruptedException {
        synchronized (lock) {
            while (buffer.size() == capacity) {
                System.out.println("Buffer full, producer waiting...");
                lock.wait(); // releases `lock` while waiting, re-acquires before returning
            }
            buffer.add(value);
            System.out.println("Produced: " + value + " (buffer size=" + buffer.size() + ")");
            lock.notifyAll(); // wake any consumer blocked on an empty buffer
        }
    }

    public int consume() throws InterruptedException {
        synchronized (lock) {
            while (buffer.isEmpty()) {
                System.out.println("Buffer empty, consumer waiting...");
                lock.wait();
            }
            int value = buffer.poll();
            System.out.println("Consumed: " + value + " (buffer size=" + buffer.size() + ")");
            lock.notifyAll(); // wake any producer blocked on a full buffer
            return value;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ProducerConsumerWaitNotify pc = new ProducerConsumerWaitNotify(3);

        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 6; i++) {
                    pc.produce(i);
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 6; i++) {
                    pc.consume();
                    Thread.sleep(150); // slower consumer, forces the buffer to fill up
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
    }
}
