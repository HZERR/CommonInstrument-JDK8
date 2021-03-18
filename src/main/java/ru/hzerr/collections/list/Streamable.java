package ru.hzerr.collections.list;

import java.util.function.Function;

public interface Streamable<T> {

    <R> HList<R> map(Function<? super T, ? extends R> mapper);
}
