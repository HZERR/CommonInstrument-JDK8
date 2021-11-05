package ru.hzerr.collections.map;

import java.util.function.Function;
import java.util.function.Predicate;

public interface Receiver<K, V> {

    /**
     * The method is supposed to return some internal state of the object.
     * Applies a function to each key and checks for the changed value.
     * Returns the changed value if and only if the predicate returns true.
     * @param function function applied to each key
     * @param predicate a predicate that checks the key
     * @param <R> the type of the return value
     * @return the internal state of the object
     */
    <R> R getStateKeyIf(Function<? super K, R> function, Predicate<R> predicate);

    /**
     * The method is supposed to return some internal state of the object
     * Checks the key and if the predicate returns true, applies the function to the key and returns the result
     * @param predicate a predicate that checks each key
     * @param function function applied to the key
     * @param <R> the type of the return value
     * @return the internal state of the object
     */
    <R> R getStateKeyIf(Predicate<K> predicate, Function<? super K, R> function);

    /**
     * It is assumed that the method will return some internal state of the object.
     * Applies the function to each value and tests the changed value.
     * Returns the changed value if and only if the predicate returns true.
     * @param function function applied to each value
     * @param predicate a predicate that checks the value
     * @param <R> the type of the return value
     * @return the internal state of the object
     */
    <R> R getStateValueIf(Function<? super V, R> function, Predicate<R> predicate);

    /**
     * The method is supposed to return some internal state of the object.
     * Checks the value and if the predicate returns true, applies the function to the value and returns the result.
     * @param predicate a predicate that checks each value
     * @param function function applied to the value
     * @param <R> the type of the return value
     * @return the internal state of the object
     */
    <R> R getStateValueIf(Predicate<V> predicate, Function<? super V, R> function);
}
