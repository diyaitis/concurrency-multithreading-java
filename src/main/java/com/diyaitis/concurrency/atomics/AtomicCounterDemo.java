package com.diyaitis.concurrency.atomics;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * AtomicInteger fixes the same race condition as `synchronized`, but
 * without ever blocking a thread. It uses CAS (Compare-And-Swap), a CPU
 * instruction that atomically does "if the value is still what I last
 * read, update it; otherwise retry" - a lock-free alternative to locking.
 *
 * For a single counter, this is both simpler and faster than synchronized,
 * because there's no lock acquisition/contention overhead. It stops
 * winning once you need to update MULTIPLE related variables together
 * atomically - that's when you actually need a lock.
 */
public class AtomicCounterDemo {

    private final AtomicInteger count = new AtomicInteger(0);

    public void increment() {
        count.incrementAndGet(); // atomic read-modify-write, no lock needed
    }

    public static void main(String[] args) throws InterruptedException {
        AtomicCounterDemo demo = new AtomicCounterDemo();
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
        System.out.println("Actual:   " + demo.count.get());

        // compareAndSet is the primitive everything above is built on:
        // "set to 0 only if it's currently still at the expected value".
        AtomicInteger flag = new AtomicInteger(1);
        boolean changed = flag.compareAndSet(1, 0);
        System.out.println("CAS 1->0 succeeded: " + changed + ", value now: " + flag.get());
        boolean changedAgain = flag.compareAndSet(1, 99); // value is 0 now, not 1 - fails
        System.out.println("CAS 1->99 succeeded: " + changedAgain + " (expected false, value unchanged)");
    }
}
