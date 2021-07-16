package ru.hzerr.collections.list;

import java.util.function.Predicate;

public interface Contender<E> {

    boolean noContains(E element);
    boolean noContains(Predicate<? super E> action);

    boolean contains(Predicate<? super E> action);
}
