package ru.hzerr.collections.list;

import ru.hzerr.stream.HStream;

import java.util.List;

public interface HList<E> extends List<E>,
        Finder<E>,
        Contender<E>,
        Streamable<E> {

    @SuppressWarnings("unchecked")
    void add(E... elements);

    @SuppressWarnings("unchecked")
    void setAll(E... elements);

    HStream<E> toHStream();
}
