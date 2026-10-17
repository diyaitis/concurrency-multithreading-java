package com.diyaitis.concurrency.producerconsumer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The same producer-consumer pattern as ProducerConsumerWaitNotify, but
 * using BlockingQueue - which already contains exactly that wait/notify
 * machinery internally. put() blocks while full, take() blocks while
 * empty. In real code, always prefer this over hand-rolled wait/notify;
 * it's less code and much harder to get subtly wrong.
 */
public class ProducerConsumerBlockingQueue {

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(3);

        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 6; i++) {
                    queue.put(i); // blocks if the queue is at capacity
                    System.out.println("Produced: " + i + " (queue size=" + queue.size() + ")");
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 6; i++) {
                    int value = queue.take(); // blocks if the queue is empty
                    System.out.println("Consumed: " + value + " (queue size=" + queue.size() + ")");
                    Thread.sleep(150); // slower consumer, forces the queue to fill up
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
