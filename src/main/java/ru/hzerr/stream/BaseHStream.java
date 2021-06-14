package ru.hzerr.stream;

import ru.hzerr.stream.function.Predicate;
import ru.hzerr.stream.function.Consumer;
import ru.hzerr.stream.function.Supplier;
import ru.hzerr.stream.function.Function;
import ru.hzerr.stream.function.BiFunction;
import ru.hzerr.stream.function.BinaryOperator;
import ru.hzerr.stream.function.BiConsumer;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.Collector;

/**
 * @author HZERR
 * @see java.util.stream.BaseStream
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public interface BaseHStream<T, S extends BaseHStream<T, S>> {

    Iterator<T> iterator();
    Spliterator<T> spliterator();

    S filter(Predicate<? super T> action);

    S peek(Consumer<? super T> action);

    T reduce(T identity, BinaryOperator<T> accumulator);
    Optional<T> reduce(BinaryOperator<T> accumulator);
    <U> U reduce(U identity,
                 BiFunction<U, ? super T, U> accumulator,
                 BinaryOperator<U> combiner);

    S forEach(Consumer<? super T> action);
    S forEachOrdered(Consumer<? super T> action);

    <R> BaseHStream<R, ?> map(Function<? super T, ? extends R> mapper);

    boolean allMatch(Predicate<? super T> action);
    boolean anyMatch(Predicate<? super T> action);
    boolean noneMatch(Predicate<? super T> action);

    S sequential();
    S parallel();
    S parallelIfNeeded();
    boolean isParallel();

    Optional<T> findFirst();
    Optional<T> findAny();

    <R, A> R collect(Collector<? super T, A, R> collector);
    <R> R collect(Supplier<R> supplier,
                  BiConsumer<R, ? super T> accumulator,
                  BiConsumer<R, R> combiner);

    Optional<T> min(Comparator<? super T> comparator);
    Optional<T> max(Comparator<? super T> comparator);

    S distinct();

    S sorted();
    S sorted(Comparator<? super T> comparator);

    S unordered();

    S skip(long n);
    S limit(long maxSize);

    long count();
}
