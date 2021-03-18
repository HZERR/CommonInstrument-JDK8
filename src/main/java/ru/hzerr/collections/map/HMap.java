package ru.hzerr.collections.map;

import java.util.Map;

public interface HMap<K, V> extends Map<K, V>,
        Finder<K, V>,
        Receiver<K, V>,
        Contender<K, V>,
        Streamable<K, V> {

    V putAndGet(K key, V value);
    V putIfAbsentAndGet(K key, V value);
}
