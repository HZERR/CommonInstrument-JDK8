package ru.hzerr.collections.list;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public interface HList<E> extends List<E>,
        Finder<E>,
        Changer<E>,
        Replacer<E>,
        Removable<E>,
        Contender<E>,
        Streamable<E> {

    /**
     * Adds elements to the collection
     * @see java.util.AbstractList#addAll(Collection)
     * @param elements items to be added to the collection
     */
    @SuppressWarnings("unchecked")
    void addAll(E... elements);

    /**
     * Clears the list and sets the elements
     * @param elements items to be added to the collection
     */
    @SuppressWarnings("unchecked")
    void setAll(E... elements);

    <C extends Collection<E>> C to(Supplier<C> collectionFactory);

    /**
     * Returns the first element of the collection or throws an IndexOutOfBoundsException
     * @throws IndexOutOfBoundsException if the first element does not exist
     * @return first element of the collection
     */
    E firstElement();

    /**
     * Returns the last element of the collection or throws an IndexOutOfBoundsException
     * @throws IndexOutOfBoundsException if the last element does not exist
     * @return last element of the collection
     */
    E lastElement();

    /**
     * Creates an {@link ArrayHList}, adds elements to it and returns the collection
     * @param elements elements to be added to the collection
     * @param <E> type of elements
     * @return a non thread-safe collection populated with the specified elements
     */
    @SafeVarargs
    static <E> HList<E> of(E... elements) { return ArrayHList.create(elements); }

    /**
     * Creates an empty {@link ArrayHList} and returns it
     * @param <E> type of elements
     * @return an empty non thread-safe collection
     */
    static <E> HList<E> newList() { return new ArrayHList<>(); }
}
