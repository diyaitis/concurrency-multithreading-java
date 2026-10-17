package com.diyaitis.concurrency.coordination;

import java.util.concurrent.CountDownLatch;

/**
 * CountDownLatch lets one or more threads wait until N other threads have
 * finished some setup. It's one-shot: once the count hits zero it never
 * resets. Classic use case: wait for several services/workers to finish
 * initializing before the app starts serving traffic.
 */
public class CountDownLatchDemo {

    public static void main(String[] args) throws InterruptedException {
        final int workerCount = 3;
        CountDownLatch startupLatch = new CountDownLatch(workerCount);

        for (int i = 1; i <= workerCount; i++) {
            final int workerId = i;
            new Thread(() -> {
                System.out.println("Worker " + workerId + " initializing...");
                sleepQuietly(100L * workerId);
                System.out.println("Worker " + workerId + " ready.");
                startupLatch.countDown(); // decrement the shared count
            }).start();
        }

        System.out.println("Main thread waiting for all workers to be ready...");
        startupLatch.await(); // blocks until the count reaches 0
        System.out.println("All " + workerCount + " workers ready - main thread proceeding.");
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
