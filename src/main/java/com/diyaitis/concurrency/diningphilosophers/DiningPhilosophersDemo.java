package com.diyaitis.concurrency.diningphilosophers;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Dining Philosophers problem: N philosophers sit around a table with
 * one fork between each pair; eating requires picking up both forks on
 * either side. If every philosopher picks up their left fork first, then
 * reaches for their right, all N can end up holding one fork each and
 * waiting forever for the other - a deadlock, for exactly the same reason
 * as DeadlockDemo, just with a cycle of N locks instead of 2.
 *
 * Fix applied here: the same technique as DeadlockAvoidanceDemo, consistent
 * lock ordering - every philosopher picks up their LOWER-numbered fork
 * first, regardless of which hand that is. That breaks the circular wait,
 * since the highest-numbered philosopher is the one odd case who ends up
 * reaching for their "right" fork first instead - removing the cycle.
 */
public class DiningPhilosophersDemo {

    private static final int PHILOSOPHER_COUNT = 5;

    public static void main(String[] args) {
        Lock[] forks = new Lock[PHILOSOPHER_COUNT];
        for (int i = 0; i < PHILOSOPHER_COUNT; i++) {
            forks[i] = new ReentrantLock();
        }

        for (int i = 0; i < PHILOSOPHER_COUNT; i++) {
            final int philosopherId = i;
            final int leftFork = i;
            final int rightFork = (i + 1) % PHILOSOPHER_COUNT;
            new Thread(() -> dine(philosopherId, forks[leftFork], forks[rightFork], leftFork, rightFork)).start();
        }
    }

    private static void dine(int philosopherId, Lock leftFork, Lock rightFork, int leftIdx, int rightIdx) {
        // Always acquire the lower-indexed fork first. For every philosopher
        // except the last, that's their left fork; for the last philosopher,
        // whose "right" fork is index 0, it's the other way around - and
        // that one inversion is exactly what breaks the circular wait.
        Lock first = leftIdx < rightIdx ? leftFork : rightFork;
        Lock second = leftIdx < rightIdx ? rightFork : leftFork;

        for (int round = 1; round <= 3; round++) {
            think(philosopherId, round);
            first.lock();
            try {
                second.lock();
                try {
                    eat(philosopherId, round);
                } finally {
                    second.unlock();
                }
            } finally {
                first.unlock();
            }
        }
        System.out.println("Philosopher " + philosopherId + " is done eating.");
    }

    private static void think(int philosopherId, int round) {
        System.out.println("Philosopher " + philosopherId + " thinking (round " + round + ")...");
        sleepQuietly(30);
    }

    private static void eat(int philosopherId, int round) {
        System.out.println("Philosopher " + philosopherId + " eating (round " + round + ")...");
        sleepQuietly(30);
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
