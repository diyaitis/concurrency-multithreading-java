package com.diyaitis.concurrency.collections;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Plain HashMap/ArrayList are not thread-safe - concurrent modification
 * can corrupt internal structure (not just "wrong values", but infinite
 * loops or ClassCastExceptions in real JDK bug reports). java.util.concurrent
 * provides drop-in replacements built for concurrent access:
 *
 *  - ConcurrentHashMap: lock-striped map, safe concurrent reads AND writes,
 *    no full-map lock like the old synchronizedMap() wrapper needed.
 *  - CopyOnWriteArrayList: every write copies the whole backing array.
 *    Reads never block and never see a torn state. Great for rarely-written,
 *    often-iterated lists (e.g. a list of event listeners); terrible if you
 *    write often (every write is an O(n) copy).
 */
public class ConcurrentCollectionsDemo {

    public static void main(String[] args) throws InterruptedException {
        Map<String, AtomicInteger> wordCounts = new ConcurrentHashMap<>();
        String[] words = {"java", "thread", "java", "lock", "thread", "java"};

        Runnable counter = () -> {
            for (String word : words) {
                // computeIfAbsent + the AtomicInteger inside is itself a common
                // concurrent-map idiom for "get-or-create, then update atomically"
                wordCounts.computeIfAbsent(word, w -> new AtomicInteger(0)).incrementAndGet();
            }
        };

        Thread t1 = new Thread(counter);
        Thread t2 = new Thread(counter);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Word counts after 2 threads each counting the same array:");
        wordCounts.forEach((word, count) -> System.out.println("  " + word + " = " + count));
        System.out.println("(expected java=6, thread=4, lock=2)");

        List<String> listeners = new CopyOnWriteArrayList<>();
        listeners.add("listenerA");
        listeners.add("listenerB");

        // Safe to iterate while another thread mutates it - the iterator sees
        // a frozen snapshot from the moment iteration started, never a
        // ConcurrentModificationException.
        Thread mutator = new Thread(() -> listeners.add("listenerC"));
        mutator.start();
        for (String listener : listeners) {
            System.out.println("notifying " + listener);
        }
        mutator.join();
        System.out.println("Final listeners: " + listeners);
    }
}
