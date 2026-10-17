package com.diyaitis.concurrency.deadlock;

/**
 * The classic deadlock: two threads each hold one lock and wait for the
 * other's lock, forever. This happens because they acquire two locks in
 * OPPOSITE order:
 *   Thread 1: lock(A) -> lock(B)
 *   Thread 2: lock(B) -> lock(A)
 *
 * Run this and it WILL hang (by design) - that's the point. Kill it with
 * Ctrl+C. See DeadlockAvoidanceDemo for the fix (consistent lock ordering).
 */
public class DeadlockDemo {

    private static final Object lockA = new Object();
    private static final Object lockB = new Object();

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            synchronized (lockA) {
                System.out.println("Thread 1: holding lock A, waiting for lock B...");
                sleepQuietly(100); // widen the window so the deadlock reliably happens
                synchronized (lockB) {
                    System.out.println("Thread 1: acquired both locks (won't print - deadlocked)");
                }
            }
        }, "Thread-1");

        Thread t2 = new Thread(() -> {
            synchronized (lockB) {
                System.out.println("Thread 2: holding lock B, waiting for lock A...");
                sleepQuietly(100);
                synchronized (lockA) {
                    System.out.println("Thread 2: acquired both locks (won't print - deadlocked)");
                }
            }
        }, "Thread-2");

        t1.start();
        t2.start();

        System.out.println("Both threads started. The JVM will hang here - that is the deadlock.");
        System.out.println("(Interrupt the process to stop it; this demo intentionally never finishes.)");
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
