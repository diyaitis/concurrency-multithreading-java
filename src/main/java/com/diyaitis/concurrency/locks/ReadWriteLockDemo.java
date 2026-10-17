package com.diyaitis.concurrency.locks;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ReentrantLock serializes EVERY access, even when ten threads just want to
 * read a value that never changes while they're reading it. ReadWriteLock
 * splits that into two locks: any number of readers can hold the read lock
 * simultaneously, but a writer needs exclusive access (blocks everyone).
 *
 * Use this when reads vastly outnumber writes - e.g. a config cache that's
 * read constantly and updated rarely.
 */
public class ReadWriteLockDemo {

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private String config = "initial-config";

    public String read() {
        rwLock.readLock().lock();
        try {
            // simulate work so overlapping reads are visible in the log
            sleepQuietly(100);
            return config;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void write(String newValue) {
        rwLock.writeLock().lock();
        try {
            config = newValue;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ReadWriteLockDemo demo = new ReadWriteLockDemo();

        long start = System.currentTimeMillis();
        Thread r1 = new Thread(() -> log("reader-1", demo.read(), start));
        Thread r2 = new Thread(() -> log("reader-2", demo.read(), start));
        Thread r3 = new Thread(() -> log("reader-3", demo.read(), start));

        // All three readers run concurrently - total time should be ~100ms,
        // not ~300ms, because readLock() doesn't block other readers.
        r1.start();
        r2.start();
        r3.start();
        r1.join();
        r2.join();
        r3.join();
        System.out.println("Three readers finished in ~" + (System.currentTimeMillis() - start) + "ms (expect ~100ms, not ~300ms)");

        demo.write("updated-config");
        System.out.println("After write: " + demo.read());
    }

    private static void log(String name, String value, long start) {
        System.out.println(name + " read '" + value + "' at +" + (System.currentTimeMillis() - start) + "ms");
    }
}
