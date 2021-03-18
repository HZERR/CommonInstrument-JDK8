package ru.hzerr.collections.map;

import java.util.function.Function;
import java.util.function.Predicate;

// CHANGE DOCUMENTATION
public interface Receiver<K, V> {

    <R> R getKIf(Function<? super K, R> function, Predicate<R> predicate);
    <R> R getKIf(Predicate<K> predicate, Function<? super K, R> function);
    <R> R getVIf(Function<? super V, R> function, Predicate<R> predicate);
    <R> R getVIf(Predicate<V> predicate, Function<? super V, R> function);
}
