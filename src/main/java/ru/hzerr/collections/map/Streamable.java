package ru.hzerr.collections.map;

import java.util.function.Consumer;

public interface Streamable<K, V> {

    /**
     * Performs the specified action for each key
     * @param action the action to be performed for each key
     */
    void forKEach(Consumer<? super K> action);

    /**
     * Performs the specified action for each value
     * @param action the action to be performed for each value
     */
    void forVEach(Consumer<? super V> action);
}
