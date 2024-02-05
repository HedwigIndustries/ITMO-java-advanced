package info.kgeorgiy.ja.kadyrov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

/**
 * Implements the ParallelMapper interface.
 *
 * @author Kadyrov Rustam.
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threadsList;
    private final Queue<Runnable> tasks = new ArrayDeque<>();

    /**
     * Creates threads of worker threads that can be used for parallelization.
     *
     * @param threads threads count
     */

    public ParallelMapperImpl(int threads) {
        this.threadsList = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            startThread();
        }
    }

    private void startThread() {
        Thread thread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    runTask();
                }
            } catch (InterruptedException ignored) {
            } finally {
                Thread.currentThread().interrupt();
            }
        });
        threadsList.add(thread);
        thread.start();
    }

    private void runTask() throws InterruptedException {
        final Runnable task;
        synchronized (tasks) {
            while (tasks.isEmpty()) {
                tasks.wait();
            }
            task = tasks.poll();
            tasks.notify();
        }
        task.run();
    }

    /**
     * Maps function {@code f} over specified {@code args}.
     * Mapping for each element performed in parallel.
     *
     * @throws InterruptedException if calling thread was interrupted
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        final List<R> result = new ArrayList<>(Collections.nCopies(args.size(), null));
        CountTask count = new CountTask(args.size());
        for (int i = 0; i < args.size(); i++) {
            final int finalI = i;
            synchronized (tasks) {
                Runnable task = () -> {
                    result.set(finalI, f.apply(args.get(finalI)));
                    count.inc();
                };
                tasks.add(task);
                tasks.notify();
            }
        }
        count.await();
        return result;
    }

    private static class CountTask {
        private int currentCount;
        private final int maxCount;

        private CountTask(int argsSize) {
            this.maxCount = argsSize;
        }

        private synchronized void inc() {
            if (++currentCount >= maxCount) {
                notify();
            }

        }

        private synchronized void await() throws InterruptedException {
            while (currentCount < maxCount) {
                wait();
            }
        }
    }

    /**
     * Stops all threads. All unfinished mappings are left in undefined state.
     */
    @Override
    public void close() {
        threadsList.forEach(thread -> {
            thread.interrupt();
            while (true) {
                try {
                    thread.join();
                    break;
                } catch (InterruptedException e) {
                    System.out.println("Expected threads join:" + e.getMessage());
                }
            }
        });

    }
}
