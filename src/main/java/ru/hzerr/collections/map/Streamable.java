package ru.hzerr.collections.map;

import java.util.function.Consumer;

public interface Streamable<K, V> {

    void forKEach(Consumer<? super K> consumer);
    void forVEach(Consumer<? super V> consumer);
}
