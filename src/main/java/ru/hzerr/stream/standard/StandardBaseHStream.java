package ru.hzerr.stream.standard;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * @author HZERR
 * @see java.util.stream.BaseStream
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface StandardBaseHStream<T, S extends StandardBaseHStream<T, S>> {

    Iterator<T> iterator();

    Spliterator<T> spliterator();

    S filter(Predicate<? super T> action);

    S peek(Consumer<? super T> action);

    T reduce(T identity, BinaryOperator<T> accumulator);
    Optional<T> reduce(BinaryOperator<T> accumulator);
    <U> U reduce(U identity,
                 BiFunction<U, ? super T, U> accumulator,
                 BinaryOperator<U> combiner);

    void forEach(Consumer<? super T> action);
    void forEachOrdered(Consumer<? super T> action);

    <R> StandardBaseHStream<R, ?> map(Function<? super T, ? extends R> mapper);

    boolean allMatch(Predicate<? super T> action);
    boolean anyMatch(Predicate<? super T> action);
    boolean noneMatch(Predicate<? super T> action);

    S sequential();
    S parallel();
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
