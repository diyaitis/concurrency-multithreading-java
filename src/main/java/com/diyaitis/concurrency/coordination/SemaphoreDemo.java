package com.diyaitis.concurrency.coordination;

import java.util.concurrent.Semaphore;

/**
 * A lock lets exactly ONE thread in. A Semaphore lets up to N threads in
 * at once - it's a counter of "permits" that acquire() decrements and
 * release() increments, blocking when the count hits zero. Classic use
 * case: capping concurrent access to a limited resource, e.g. at most 2
 * threads hitting a rate-limited external API at a time.
 */
public class SemaphoreDemo {

    public static void main(String[] args) {
        final int permits = 2;
        Semaphore pool = new Semaphore(permits);

        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            new Thread(() -> accessLimitedResource(taskId, pool)).start();
        }
    }

    private static void accessLimitedResource(int taskId, Semaphore pool) {
        try {
            System.out.println("Task " + taskId + " waiting for a permit...");
            pool.acquire(); // blocks if no permits (slots) are free
            try {
                System.out.println("Task " + taskId + " acquired a permit, working. Permits left: " + pool.availablePermits());
                Thread.sleep(200);
            } finally {
                pool.release(); // ALWAYS release in finally, same reasoning as unlock()
                System.out.println("Task " + taskId + " released its permit.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
