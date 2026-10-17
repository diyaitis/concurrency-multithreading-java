package com.diyaitis.concurrency.threads;

/**
 * The three common ways to start a thread in Java, and why you almost
 * always want the Runnable/lambda forms rather than extending Thread
 * (extending Thread burns your only superclass slot and couples the
 * "what to run" to "how it runs").
 */
public class ThreadCreationDemo {

    static class WorkerThread extends Thread {
        @Override
        public void run() {
            System.out.println("[extends Thread] running on " + Thread.currentThread().getName());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // 1. Subclassing Thread
        Thread t1 = new WorkerThread();
        t1.start();

        // 2. Implementing Runnable (preferred: separates the task from the execution mechanism)
        Runnable task = () -> System.out.println("[Runnable] running on " + Thread.currentThread().getName());
        Thread t2 = new Thread(task, "runnable-thread");
        t2.start();

        // 3. Lambda directly
        Thread t3 = new Thread(() ->
                System.out.println("[lambda] running on " + Thread.currentThread().getName()));
        t3.start();

        // join() blocks the calling thread until the target thread finishes.
        // Without these joins, main() could exit before the workers print anything.
        t1.join();
        t2.join();
        t3.join();

        System.out.println("All threads finished. Main thread: " + Thread.currentThread().getName());
    }
}
