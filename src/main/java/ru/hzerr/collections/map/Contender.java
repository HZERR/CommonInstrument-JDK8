package ru.hzerr.collections.map;

import java.util.function.Predicate;

public interface Contender<K, V> {

    boolean containsKey(Predicate<K> predicate);
    boolean containsValue(Predicate<V> predicate);

    boolean noContainsKey(K key);
    boolean noContainsValue(V value);

    boolean noContainsKey(Predicate<K> predicate);
    boolean noContainsValue(Predicate<K> predicate);
}
