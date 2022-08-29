package ru.hzerr.collections.list;

import java.util.function.Predicate;

public interface Contender<E> {

    /**
     * @see java.util.List#contains(Object)
     * @param element element, the absence of which in this list should be checked
     * @return true if the collection does not contain a value
     */
    boolean noContains(E element);

    /**
     * Checks that all items in the given collection do not match this condition
     * @see Contender#contains(Predicate)
     * @param action condition to check the elements
     * @return true if and only if all tests over the elements return false
     */
    boolean noContains(Predicate<? super E> action);

    /**
     * Checks that all items in the given collection match the given condition
     * @see Contender#noContains(Predicate)
     * @param action condition to check the elements
     * @return true if and only if all tests over the elements return true
     */
    boolean contains(Predicate<? super E> action);
    boolean containsAll(E... elements);
    boolean noContainsAll(E... elements);
}
