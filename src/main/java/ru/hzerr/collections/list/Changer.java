package ru.hzerr.collections.list;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Changer<T> {

    /**
     * Checks all elements in the collection, and if the element satisfies the condition, it is changed
     * @param condition condition to check the elements of the collection
     * @param changer element change function
     */
    void changeIf(Predicate<? super T> condition, Consumer<? super T> changer);
}
