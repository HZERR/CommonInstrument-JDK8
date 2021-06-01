package ru.hzerr.collections.list;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

public interface Finder<T> {

    Optional<T> find(Predicate<T> predicate);
    Collection<T> findAll(Predicate<T> predicate);
}
