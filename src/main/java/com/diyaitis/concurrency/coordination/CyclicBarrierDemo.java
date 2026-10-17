package com.diyaitis.concurrency.coordination;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * CyclicBarrier is like CountDownLatch's reusable cousin: N threads all
 * call await(), and NONE of them proceeds until ALL N have arrived at the
 * barrier - then it resets automatically for the next round. Classic use
 * case: simulations or multi-phase algorithms where every worker must
 * finish phase 1 before anyone starts phase 2.
 */
public class CyclicBarrierDemo {

    public static void main(String[] args) {
        final int workerCount = 3;

        // The optional barrierAction runs once, on one of the worker threads,
        // exactly when the last thread arrives - useful for merging per-worker
        // results before the next phase starts.
        CyclicBarrier barrier = new CyclicBarrier(workerCount,
                () -> System.out.println(">>> All workers reached the barrier, starting next phase <<<"));

        for (int i = 1; i <= workerCount; i++) {
            final int workerId = i;
            new Thread(() -> runPhases(workerId, barrier)).start();
        }
    }

    private static void runPhases(int workerId, CyclicBarrier barrier) {
        try {
            System.out.println("Worker " + workerId + " doing phase 1 work...");
            Thread.sleep(100L * workerId);
            System.out.println("Worker " + workerId + " finished phase 1, waiting at barrier.");
            barrier.await(); // blocks until every worker has called await()

            System.out.println("Worker " + workerId + " doing phase 2 work...");
            Thread.sleep(50);
            System.out.println("Worker " + workerId + " finished phase 2, waiting at barrier.");
            barrier.await(); // the SAME barrier is reused for the second round

            System.out.println("Worker " + workerId + " done.");
        } catch (InterruptedException | BrokenBarrierException e) {
            Thread.currentThread().interrupt();
        }
    }
}
