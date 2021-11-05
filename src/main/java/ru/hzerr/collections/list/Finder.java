package ru.hzerr.collections.list;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

public interface Finder<T> {

    /**
     * Checks all elements in the collection. Returns the first found element satisfying the condition
     * @param predicate search condition
     * @return {@link Optional#empty()} if the element satisfying the condition is not found, otherwise the element itself
     */
    Optional<T> find(Predicate<? super T> predicate);

    /**
     * Checks all elements in the collection. Returns all elements satisfying the condition
     * @param predicate search condition
     * @return a collection with elements satisfying the condition, and if there are no such elements, it returns an empty collection
     */
    Collection<T> findAll(Predicate<? super T> predicate);
}
