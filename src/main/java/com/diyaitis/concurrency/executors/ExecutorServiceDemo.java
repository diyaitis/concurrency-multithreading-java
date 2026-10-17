package com.diyaitis.concurrency.executors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Manually creating a new Thread per task doesn't scale - thread creation
 * is expensive, and nothing stops you from spawning 10,000 threads and
 * exhausting the OS. ExecutorService manages a pool of reusable threads
 * and a queue of pending tasks instead.
 *
 * Runnable has no return value and can't throw a checked exception.
 * Callable<T> returns a value and can throw - submit() gives you back a
 * Future<T> you can block on with get().
 */
public class ExecutorServiceDemo {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // Fixed pool: exactly N worker threads, extra tasks queue up and wait.
        // Good default for CPU-bound or steady-load work where you want a hard cap.
        ExecutorService fixedPool = Executors.newFixedThreadPool(4);

        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            final int taskId = i;
            Callable<Integer> task = () -> {
                System.out.println("Task " + taskId + " running on " + Thread.currentThread().getName());
                Thread.sleep(50);
                return taskId * taskId;
            };
            futures.add(fixedPool.submit(task));
        }

        int sum = 0;
        for (Future<Integer> f : futures) {
            sum += f.get(); // blocks until that task's result is ready
        }
        System.out.println("Sum of squares 1..8 = " + sum + " (expected 204)");

        // Shutting down is NOT automatic - a live ExecutorService keeps the JVM alive.
        fixedPool.shutdown();
        if (!fixedPool.awaitTermination(5, TimeUnit.SECONDS)) {
            fixedPool.shutdownNow(); // force-cancel anything still running
        }

        // Cached pool: creates threads on demand, reuses idle ones, kills ones
        // idle for 60s. Good for lots of short-lived, bursty, I/O-bound tasks -
        // bad for CPU-bound work, since it won't cap concurrency.
        ExecutorService cachedPool = Executors.newCachedThreadPool();
        cachedPool.execute(() -> System.out.println("cached-pool task on " + Thread.currentThread().getName()));
        cachedPool.shutdown();
    }
}
