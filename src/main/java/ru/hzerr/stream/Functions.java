package ru.hzerr.stream;

import ru.hzerr.stream.function.BiFilter;
import ru.hzerr.stream.function.BiForEach;
import ru.hzerr.stream.function.BiMap;

public interface Functions<T> extends BiForEach<T>, BiMap<T>, BiFilter<T> {

    boolean contentEquals(Object o);
    int contentHashCode();
}
