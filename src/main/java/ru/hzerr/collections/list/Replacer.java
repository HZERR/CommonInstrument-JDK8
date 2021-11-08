package ru.hzerr.collections.list;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public interface Replacer<T> {

    /**
     * Checks all elements of the collection and if the element satisfies the condition, it is replaced
     * @param condition condition to check the elements of the collection
     */
    void replaceIf(Predicate<? super T> condition, T replacement);
    void replaceIf(Predicate<? super T> condition, UnaryOperator<T> replacer);
}
