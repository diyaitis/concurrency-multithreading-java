package com.diyaitis.concurrency.completablefuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Future.get() blocks with no way to chain further work or combine results
 * without blocking somewhere. CompletableFuture fixes that: it's a
 * composable, non-blocking pipeline for async work - the async equivalent
 * of chaining .then() in JavaScript promises.
 */
public class CompletableFutureDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // supplyAsync runs on the common ForkJoinPool by default, off the main thread
        CompletableFuture<String> future = CompletableFuture
                .supplyAsync(() -> {
                    System.out.println("Fetching user id on " + Thread.currentThread().getName());
                    sleepQuietly(100);
                    return 42;
                })
                // thenApply transforms the result once it's ready - still async, no blocking
                .thenApply(userId -> {
                    System.out.println("Looking up name for user " + userId + " on " + Thread.currentThread().getName());
                    sleepQuietly(100);
                    return "user-" + userId;
                })
                .thenApply(name -> name.toUpperCase());

        System.out.println("Main thread kept running while the pipeline executes in the background.");
        System.out.println("Final result: " + future.get()); // only blocks here, at the very end

        // Combining two independent async calls without blocking on either individually
        CompletableFuture<Integer> priceFuture = CompletableFuture.supplyAsync(() -> {
            sleepQuietly(100);
            return 25;
        });
        CompletableFuture<Integer> quantityFuture = CompletableFuture.supplyAsync(() -> {
            sleepQuietly(150);
            return 4;
        });

        CompletableFuture<Integer> totalFuture = priceFuture.thenCombine(quantityFuture, (price, qty) -> price * qty);
        System.out.println("Total = " + totalFuture.get() + " (expected 100)");

        // exceptionally gives you a fallback instead of letting get() throw
        CompletableFuture<Integer> risky = CompletableFuture
                .<Integer>supplyAsync(() -> {
                    throw new RuntimeException("simulated failure");
                })
                .exceptionally(ex -> {
                    System.out.println("Recovered from: " + ex.getMessage());
                    return -1;
                });
        System.out.println("Risky result: " + risky.get() + " (expected -1)");
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
