package ru.hzerr.collections.map;

import java.util.function.Predicate;

public interface Finder<K, V> {

    K findKey(Predicate<K> predicate);
    V findValue(Predicate<V> predicate);
}
