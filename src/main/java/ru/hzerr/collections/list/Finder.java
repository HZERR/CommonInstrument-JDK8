package ru.hzerr.collections.list;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

public interface Finder<T> {

    Optional<T> find(Predicate<? super T> predicate);
    Collection<T> findAll(Predicate<? super T> predicate);
}
