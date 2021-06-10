package ru.hzerr.stream.bi;

import ru.hzerr.stream.HStream;

@Deprecated
public class DoubleHStreamBuilder<T1, T2> {

    private Class<T1> t1;
    private Class<T2> t2;

    public DoubleHStreamBuilder(Class<T1> t1Class, Class<T2> t2Class) {
        this.t1 = t1Class;
        this.t2 = t2Class;
    }

    public static <T1, T2> DoubleHStreamBuilder<T1, T2> create(Class<T1> t1, Class<T2> t2) {
        return new DoubleHStreamBuilder<>(t1, t2);
    }

    public DoubleHStream<T1, T2> of(HStream<T1> stream, HStream<T2> stream2) {
        return new DoubleHStream<>(t1, t2, stream, stream2);
    }
}
