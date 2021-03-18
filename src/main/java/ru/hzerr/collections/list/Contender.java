package ru.hzerr.collections.list;

import java.util.function.Predicate;

public interface Contender<E> {

    boolean noContains(E element);
    boolean noContains(Predicate<E> action);

    boolean contains(Predicate<E> action);
}
