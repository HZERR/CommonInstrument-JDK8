package ru.hzerr.collections.list;

import java.util.Collection;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;
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

    @Override
    HList<E> subList(int fromIndex, int toIndex);

    /**
     * Returns the first element of the collection or throws an IndexOutOfBoundsException
     * @throws IndexOutOfBoundsException if the first element does not exist
     * @return first element of the collection
     */
    E firstElement();

    @Override
    E[] toArray();

    /**
     * Returns an array containing all of the elements in this collection,
     * using the provided {@code generator} function to allocate the returned array.
     *
     * <p>If this collection makes any guarantees as to what order its elements
     * are returned by its iterator, this method must return the elements in
     * the same order.
     *
     * @apiNote
     * This method acts as a bridge between array-based and collection-based APIs.
     * It allows creation of an array of a particular runtime type. Use
     * {@link #toArray()} to create an array whose runtime type is {@code Object[]},
     * or use {@link #toArray(Object[]) toArray(T[])} to reuse an existing array.
     *
     * <p>Suppose {@code x} is a collection known to contain only strings.
     * The following code can be used to dump the collection into a newly
     * allocated array of {@code String}:
     *
     * <pre>
     *     String[] y = x.toArray(String[]::new);</pre>
     *
     * @implSpec
     * The default implementation calls the generator function with zero
     * and then passes the resulting array to {@link #toArray(Object[]) toArray(T[])}.
     *
     * @param <T> the component type of the array to contain the collection
     * @param generator a function which produces a new array of the desired
     *                  type and the provided length
     * @return an array containing all of the elements in this collection
     * @throws ArrayStoreException if the runtime type of any element in this
     *         collection is not assignable to the {@linkplain Class#getComponentType
     *         runtime component type} of the generated array
     * @throws NullPointerException if the generator function is null
     */
    <T> T[] toArray(IntFunction<T[]> generator);


    /**
     * Returns the last element of the collection or throws an IndexOutOfBoundsException
     * @throws IndexOutOfBoundsException if the last element does not exist
     * @return last element of the collection
     */
    E lastElement();

    /**
     * Creates a new list with items that satisfy a certain condition
     * The changes do not appear on the current list
     * If the condition returns true, the item is added to the new list
     * @param condition condition that divides the elements into two groups
     * @return new list with elements satisfying the condition
     */
    HList<E> subList(Predicate<E> condition);

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
