# Concurrency & Multithreading in Java

A set of runnable, self-contained demos covering Java's concurrency toolkit,
from raw `Thread` up to `CompletableFuture`. Every demo has a `main()` method,
prints what it's doing as it runs, and every claim made in its comments has
been run and checked against the real output.

## Why each topic matters (and where to find it)

| Topic | Package | What it shows |
|---|---|---|
| Creating threads | [`threads`](src/main/java/com/diyaitis/concurrency/threads) | The 3 ways to start a thread, and why `Runnable`/lambda beats extending `Thread` |
| Race conditions | [`racecondition`](src/main/java/com/diyaitis/concurrency/racecondition) | `count++` is not atomic - two threads lose increments silently |
| `synchronized` | [`synchronization`](src/main/java/com/diyaitis/concurrency/synchronization) | Fixing the race with intrinsic locks (method and block form) |
| `ReentrantLock` / `ReadWriteLock` | [`locks`](src/main/java/com/diyaitis/concurrency/locks) | Explicit locks: `tryLock` with timeout, and letting many readers run at once |
| `ExecutorService` | [`executors`](src/main/java/com/diyaitis/concurrency/executors) | Thread pools instead of manual `Thread` management; `Callable` + `Future` |
| `AtomicInteger` | [`atomics`](src/main/java/com/diyaitis/concurrency/atomics) | Lock-free thread safety via CAS (compare-and-swap) |
| Concurrent collections | [`collections`](src/main/java/com/diyaitis/concurrency/collections) | `ConcurrentHashMap` and `CopyOnWriteArrayList` vs. their unsafe plain equivalents |
| Coordination primitives | [`coordination`](src/main/java/com/diyaitis/concurrency/coordination) | `CountDownLatch` (wait for N), `CyclicBarrier` (reusable rendezvous), `Semaphore` (cap concurrent access) |
| Producer-consumer | [`producerconsumer`](src/main/java/com/diyaitis/concurrency/producerconsumer) | Hand-rolled with `wait()`/`notifyAll()`, then the same thing with `BlockingQueue` |
| `CompletableFuture` | [`completablefuture`](src/main/java/com/diyaitis/concurrency/completablefuture) | Non-blocking async pipelines: `thenApply`, `thenCombine`, `exceptionally` |
| Deadlock | [`deadlock`](src/main/java/com/diyaitis/concurrency/deadlock) | A real deadlock from opposite lock ordering, and the fix (consistent ordering) |
| Dining Philosophers | [`diningphilosophers`](src/main/java/com/diyaitis/concurrency/diningphilosophers) | The classic N-way deadlock problem, solved with the same ordering technique at scale |

## Core ideas, in plain terms

**Why concurrency is hard at all**: most bugs here come from one fact -
operations that look like one step in source code (`count++`, "read then
write a field") are actually multiple CPU steps, and the OS can switch
between threads *between* those steps. Two threads interleaving those steps
is what produces wrong results that vary between runs of the exact same code.

**The fix is always some form of "make it impossible to interleave"**:
- `synchronized` / `ReentrantLock`: only one thread may run the guarded
  section at a time - everyone else blocks and waits.
- `AtomicInteger` and friends: no blocking at all - the CPU's compare-and-swap
  instruction does the read-modify-write as one indivisible step.
- Concurrent collections: the same idea, already built into a `Map`/`List`.

**Deadlock is the cost of using locks**: if two threads can each hold a lock
the other one wants, and neither will let go until it gets the other lock,
both wait forever. The fix used throughout this repo - and the one most
worth remembering - is: **always acquire multiple locks in the same global
order, everywhere in your codebase.** That alone makes a circular wait
mathematically impossible.

**`wait()`/`notify()` vs. higher-level tools**: `wait()`/`notifyAll()` are
the low-level primitives that things like `BlockingQueue`, `CountDownLatch`,
and `Semaphore` are built from. The producer-consumer package shows both
versions side by side on purpose - so it's clear what `BlockingQueue` is
doing for you, and why you should reach for the built-in tool instead of
hand-rolling it in real code.

**`CompletableFuture` solves a different problem**: it's not about
protecting shared state, it's about *composing* asynchronous work without
blocking a thread at every step - chaining what happens when an async result
arrives, the way `.then()` works for promises in JavaScript.

## Running the demos

Requires JDK 17+. Each class is independently runnable - no shared `main`.

```bash
mvn compile
java -cp target/classes com.diyaitis.concurrency.racecondition.RaceConditionDemo
java -cp target/classes com.diyaitis.concurrency.synchronization.SynchronizedCounterDemo
java -cp target/classes com.diyaitis.concurrency.atomics.AtomicCounterDemo
# ...and so on for any class listed in the table above
```

One demo is deliberately pathological: `deadlock.DeadlockDemo` reproduces a
real deadlock and **will hang forever** - that's the point (interrupt it with
Ctrl+C). Its fixed counterpart is `deadlock.DeadlockAvoidanceDemo`.
