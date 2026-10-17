package com.diyaitis.concurrency.racecondition;

/**
 * Demonstrates a classic race condition: count++ is NOT atomic. It is
 * read-modify-write (read count, add 1, write count back), and two threads
 * can interleave those three steps and silently lose an increment.
 *
 * Run this a few times - the final value is almost never 200_000, and it
 * changes between runs, which is exactly the symptom of a race condition
 * (non-deterministic output from the same code).
 */
public class RaceConditionDemo {

    private static int count = 0;

    public static void main(String[] args) throws InterruptedException {
        final int incrementsPerThread = 100_000;

        Runnable incrementTask = () -> {
            for (int i = 0; i < incrementsPerThread; i++) {
                count++; // NOT thread-safe
            }
        };

        Thread t1 = new Thread(incrementTask);
        Thread t2 = new Thread(incrementTask);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Expected: " + (2 * incrementsPerThread));
        System.out.println("Actual:   " + count);
        System.out.println("See synchronization.SynchronizedCounterDemo and atomics.AtomicCounterDemo for the fixes.");
    }
}
