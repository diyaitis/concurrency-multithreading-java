package com.diyaitis.concurrency.deadlock;

/**
 * Same setup as DeadlockDemo, but fixed with the simplest and most common
 * deadlock-avoidance technique: a GLOBAL, CONSISTENT lock ordering. If
 * every thread that needs both locks always acquires A before B, there's
 * no cycle possible, so no deadlock - a thread waiting for B can never be
 * holding A while another thread waiting for A holds B.
 *
 * Other real techniques, not shown here: tryLock() with a timeout and
 * backoff (fail and retry instead of waiting forever), or acquiring all
 * needed locks via a single higher-level lock object.
 */
public class DeadlockAvoidanceDemo {

    private static final Object lockA = new Object();
    private static final Object lockB = new Object();

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> acquireBothInOrder("Thread-1"), "Thread-1");
        Thread t2 = new Thread(() -> acquireBothInOrder("Thread-2"), "Thread-2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Both threads finished cleanly - no deadlock, because both always lock A before B.");
    }

    // Both callers use this SAME method, so the acquisition order can never diverge.
    private static void acquireBothInOrder(String name) {
        synchronized (lockA) {
            System.out.println(name + ": holding lock A, waiting for lock B...");
            sleepQuietly(100);
            synchronized (lockB) {
                System.out.println(name + ": acquired both locks, doing work.");
            }
        }
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
