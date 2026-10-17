package com.diyaitis.concurrency.synchronization;

/**
 * Fixes RaceConditionDemo using `synchronized`. Every thread must acquire
 * the object's intrinsic lock (monitor) before entering a synchronized
 * block/method, and only one thread can hold a given lock at a time - so
 * the read-modify-write of count++ can no longer be interleaved.
 */
public class SynchronizedCounterDemo {

    private int count = 0;

    // synchronized method == synchronized(this) { ...entire body... }
    public synchronized void increment() {
        count++;
    }

    // Equivalent, more explicit form - useful when you only need to guard
    // part of a method, or want to lock on something other than `this`.
    private final Object lock = new Object();
    private int countViaBlock = 0;

    public void incrementViaBlock() {
        synchronized (lock) {
            countViaBlock++;
        }
    }

    public int getCount() {
        return count;
    }

    public static void main(String[] args) throws InterruptedException {
        SynchronizedCounterDemo demo = new SynchronizedCounterDemo();
        final int incrementsPerThread = 100_000;

        Runnable task = () -> {
            for (int i = 0; i < incrementsPerThread; i++) {
                demo.increment();
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Expected: " + (2 * incrementsPerThread));
        System.out.println("Actual:   " + demo.getCount());
        System.out.println("Deterministic now - always matches, because `synchronized` serializes access.");
    }
}
