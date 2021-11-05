package ru.hzerr.collections.list;

import java.util.function.Function;

public interface Streamable<T> {

    /**
     * @see java.util.stream.Stream#map(Function)
     */
    <R> HList<R> map(Function<? super T, ? extends R> mapper);
}
