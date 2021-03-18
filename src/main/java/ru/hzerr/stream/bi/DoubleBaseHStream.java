package ru.hzerr.stream.bi;

import ru.hzerr.stream.function.BinaryOperator;
import ru.hzerr.stream.function.Consumer;
import ru.hzerr.stream.function.Predicate;

import java.util.Comparator;
import java.util.Optional;

public interface DoubleBaseHStream<TYPE, TYPE2, S extends DoubleBaseHStream<TYPE, TYPE2, S>> {

    <R> S filter(Class<? extends R> clazz, Predicate<? super R> action);
    <R> S forEach(Class<? extends R> clazz, Consumer<? super R> action);
    <R> S forEachOrdered(Class<? extends R> clazz, Consumer<? super R> action);
    <R> S peek(Class<? extends R> clazz, Consumer<? super R> action);

    <R> boolean allMatch(Class<? extends R> clazz, Predicate<? super R> action);
    <R> boolean anyMatch(Class<? extends R> clazz, Predicate<? super R> action);
    <R> boolean noneMatch(Class<? extends R> clazz, Predicate<? super R> action);

    <R> S parallel(Class<? extends R> clazz);
    <R> boolean isParallel(Class<? extends R> clazz);

    <R> Optional<R> findFirst(Class<? extends R> clazz);
    <R> Optional<R> findAny(Class<? extends R> clazz);

    <R> Optional<R> reduce(Class<? extends R> clazz, BinaryOperator<R> accumulator);

    <R> Optional<R> min(Class<? extends R> clazz, Comparator<? super R> comparator);
    <R> Optional<R> max(Class<? extends R> clazz, Comparator<? super R> comparator);

    <R> S distinct(Class<? extends R> clazz);

    <R> S sorted(Class<? extends R> clazz);
    <R> S sorted(Class<? extends R> clazz, Comparator<? super R> comparator);

    <R> S unordered(Class<? extends R> clazz);

    <R> S skip(Class<? extends R> clazz, long n);
    <R> S limit(Class<? extends R> clazz, long maxSize);

    <R> long count(Class<? extends R> clazz);
}
