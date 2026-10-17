package com.diyaitis.concurrency.locks;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock does the same job as `synchronized` but as an explicit
 * object, which buys you things synchronized can't do:
 *  - tryLock() with a timeout, instead of blocking forever
 *  - interruptible lock acquisition (lockInterruptibly)
 *  - inspectable state (isLocked, getHoldCount)
 *  - multiple Condition objects per lock (see ReadWriteLockDemo's cousin topics)
 *
 * The one rule that matters: unlock() MUST be in a finally block, or an
 * exception between lock() and unlock() leaves the lock held forever.
 */
public class ReentrantLockDemo {

    private final Lock lock = new ReentrantLock();
    private int count = 0;

    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock(); // always release, even if the body throws
        }
    }

    public boolean incrementIfAvailableWithin(long millis) throws InterruptedException {
        // tryLock lets a thread give up instead of blocking indefinitely -
        // synchronized has no equivalent of this.
        if (lock.tryLock(millis, java.util.concurrent.TimeUnit.MILLISECONDS)) {
            try {
                count++;
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false;
    }

    public static void main(String[] args) throws InterruptedException {
        ReentrantLockDemo demo = new ReentrantLockDemo();
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
        System.out.println("Actual:   " + demo.count);

        // demonstrate tryLock giving up gracefully instead of blocking forever.
        // Must hold the lock from a DIFFERENT thread - ReentrantLock is reentrant,
        // so the same thread re-acquiring its own lock would just succeed.
        Thread holder = new Thread(() -> {
            demo.lock.lock();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            } finally {
                demo.lock.unlock();
            }
        });
        holder.start();
        Thread.sleep(50); // let the holder thread grab the lock first
        boolean acquired = demo.incrementIfAvailableWithin(200);
        System.out.println("tryLock while held by another thread -> acquired=" + acquired + " (expected false)");
        holder.join();
    }
}
