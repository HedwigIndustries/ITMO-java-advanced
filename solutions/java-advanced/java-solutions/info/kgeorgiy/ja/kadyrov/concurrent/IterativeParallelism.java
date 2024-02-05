package info.kgeorgiy.ja.kadyrov.concurrent;


import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Scalar iterative parallelism support.
 *
 * @author Kadyrov Rustam.
 */
public class IterativeParallelism implements ScalarIP {

    ParallelMapper mapper;

    public IterativeParallelism() {
        this.mapper = null;
    }

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }


    private <T> List<List<? extends T>> getArrayParts(List<? extends T> values, int threadsCount) {
        List<List<? extends T>> arrayParts = new ArrayList<>();
        int tail = values.size() % threadsCount;
        int batchSize = values.size() / threadsCount;
        int from, to = 0;
        int j = 0;
        for (int i = 0; i < threadsCount; i++) {
            from = to;
            to = from + batchSize;
            to = (j < tail) ? to + 1 : to;
            j++;
            arrayParts.add(values.subList(from, to));
        }
        return arrayParts;
    }

    private <T, R> R runThreads(int threads, List<? extends T> values, Function<List<? extends T>, R> function,
                                Function<Stream<R>, R> getAnswer) throws InterruptedException {
        if (values == null || values.isEmpty()) {
            throw new NoSuchElementException("List is empty.");
        }
        threads = Integer.min(threads, values.size());
        List<List<? extends T>> arrayParts = getArrayParts(values, threads);
        List<R> results = (mapper == null) ? customMap(threads, function, arrayParts)
                : mapper.map(function, arrayParts);
        return getAnswer.apply(results.stream());
    }

    private static <T, R> List<R> customMap(int threads, Function<List<? extends T>, R> function,
                                            List<List<? extends T>> arrayParts) throws InterruptedException {
        List<R> results = new ArrayList<>();
        List<Thread> threadsList = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            results.add(null);
        }
        for (int i = 0; i < threads; i++) {
            int finalI = i;
            Thread thread = new Thread(() -> {
                results.set(finalI, function.apply(arrayParts.get(finalI)));
            });
            threadsList.add(thread);
            thread.start();
        }
        for (Thread thread : threadsList) {
            thread.join();
        }
        return results;
    }

    /**
     * Returns maximum value.
     *
     * @param threads    number of concurrent threads.
     * @param values     values to get maximum of.
     * @param comparator value comparator.
     * @param <T>        value type.
     * @return maximum of given values
     * @throws InterruptedException             if executing thread was interrupted.
     * @throws java.util.NoSuchElementException if no values are given.
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return runThreads(
                threads,
                values,
                list -> list.stream().max(comparator).orElseThrow(),
                stream -> stream.max(comparator).orElseThrow()
        );
        // return  mapper.map((stream) -> stream.max(comparator).orElseThrow(),)
    }

    /**
     * Returns minimum value.
     *
     * @param threads    number of concurrent threads.
     * @param values     values to get minimum of.
     * @param comparator value comparator.
     * @param <T>        value type.
     * @return minimum of given values
     * @throws InterruptedException             if executing thread was interrupted.
     * @throws java.util.NoSuchElementException if no values are given.
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return runThreads(
                threads,
                values,
                list -> list.stream().min(comparator).orElseThrow(),
                (stream) -> stream.min(comparator).orElseThrow()
        );
    }

    /**
     * Returns whether all values satisfy predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @param <T>       value type.
     * @return whether all values satisfy predicate or {@code true}, if no values are given.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return runThreads(
                threads,
                values,
                list -> list.stream().allMatch(predicate),
                stream -> stream.reduce((a, b) -> (a && b)).orElse(true)
        );
    }

    /**
     * Returns whether any of values satisfies predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @param <T>       value type.
     * @return whether any value satisfies predicate or {@code false}, if no values are given.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return runThreads(
                threads,
                values,
                list -> list.stream().anyMatch(predicate),
                (stream) -> stream.reduce((a, b) -> (a || b)).orElse(false)
        );
    }

    /**
     * Returns number of values satisfying predicate.
     *
     * @param threads   number of concurrent threads.
     * @param values    values to test.
     * @param predicate test predicate.
     * @param <T>       value type.
     * @return number of values satisfying predicate.
     * @throws InterruptedException if executing thread was interrupted.
     */
    @Override
    public <T> int count(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return runThreads(
                threads,
                values,
                list -> Long.valueOf(list.stream().filter(predicate).count()).intValue(),
                (stream) -> stream.reduce(Integer::sum).orElse(0)
        );
    }
}
