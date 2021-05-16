package ru.hzerr.stream.standard;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * A base class that allows you to reuse {@link java.util.stream.Stream}
 * @author HZERR
 * @version 1.0
 * @since 15
 */
@SuppressWarnings({"unchecked", "unused", "MethodDoesntCallSuperMethod", "DuplicatedCode"})
public class StandardHStream<T> implements StandardBaseHStream<T, StandardHStream<T>>, Cloneable {

    private StandardHStream() { this.value = Stream::empty; }
    private StandardHStream(T... values) { this.value = () -> Stream.of(values); }
    private StandardHStream(Stream<T> stream) { this.value = () -> stream; }
    private StandardHStream(Supplier<Stream<T>> value, boolean isParallel) {
        this.value = value;
        if (isParallel) {
            parallel();
        }
    }

    private Supplier<Stream<T>> value;

    @Override
    public StandardHStream<T> filter(Predicate<? super T> action) {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().filter(action);
        return this;
    }

    @Override
    public StandardHStream<T> peek(Consumer<? super T> action) {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().peek(action);
        return this;
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return this.value.get().reduce(identity, accumulator);
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return this.value.get().reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return this.value.get().reduce(identity, accumulator, combiner);
    }

    @Override
    public boolean allMatch(Predicate<? super T> action) { return this.value.get().allMatch(action); }
    @Override
    public boolean anyMatch(Predicate<? super T> action) { return this.value.get().anyMatch(action); }
    @Override
    public boolean noneMatch(Predicate<? super T> action) { return this.value.get().noneMatch(action); }

    @Override
    public void forEach(Consumer<? super T> action) { this.value.get().forEach(action); }

    @Override
    public void forEachOrdered(Consumer<? super T> action) { this.value.get().forEachOrdered(action); }

    @Override
    public <R> StandardHStream<R> map(Function<? super T, ? extends R> mapper) {
        Supplier<Stream<T>> current = this.value;
        return new StandardHStream<>(current.get().map(mapper));
    }

    @Override
    public Optional<T> findFirst() { return this.value.get().findFirst(); }
    @Override
    public Optional<T> findAny() { return this.value.get().findAny(); }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) { return this.value.get().collect(collector); }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return this.value.get().collect(supplier, accumulator, combiner);
    }

    @Override
    public Optional<T> min(Comparator<? super T> comparator) { return this.value.get().min(comparator); }

    @Override
    public Optional<T> max(Comparator<? super T> comparator) { return this.value.get().max(comparator); }

    @Override
    public StandardHStream<T> distinct() {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().distinct();
        return this;
    }

    @Override
    public StandardHStream<T> sorted() {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().sorted();
        return this;
    }

    @Override
    public StandardHStream<T> sorted(Comparator<? super T> comparator) {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().sorted(comparator);
        return this;
    }

    @Override
    public StandardHStream<T> unordered() {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().unordered();
        return this;
    }

    @Override
    public StandardHStream<T> skip(long n) {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().skip(n);
        return this;
    }

    @Override
    public StandardHStream<T> limit(long maxSize) {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().limit(maxSize);
        return this;
    }

    @Override
    public long count() { return this.value.get().count(); }

    @Override
    public StandardHStream<T> parallel() {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().parallel();
        return this;
    }

    @Override
    public boolean isParallel() { return this.value.get().isParallel(); }

    /**
     * Adds 1 item to the stream by creating a new stream
     * The operation is slow and not recommended for frequent use
     * @param t element
     * @return modified instance of HStream
     */
    public StandardHStream<T> put(T t) {
        T[] tmp = (T[]) this.value.get().toArray();
        T[] newArray = Arrays.copyOf(tmp, tmp.length + 1);
        newArray[tmp.length] = t;
        if (isParallel()) {
            this.value = () -> Stream.of(newArray);
            this.parallel();
        } else this.value = () -> Stream.of(newArray);
        return this;
    }

    /**
     * Clones the object, creating a new thread identical to the instance
     * @return a clone of this instance
     */
    @Override
    public StandardHStream<T> clone() { return new StandardHStream<>(value, isParallel()); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StandardHStream)) return false;
        StandardHStream<T> hStream = (StandardHStream<T>) o;
        T[] current = (T[]) this.value.get().toArray();
        T[] target = (T[]) hStream.value.get().toArray();
        this.value = () -> Stream.of(current);
        hStream.value = () -> Stream.of(target);
        boolean parallelIdentity = ((StandardHStream<?>) o).isParallel() == isParallel();
        return Arrays.equals(current, target) && parallelIdentity;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + Arrays.hashCode(this.value.get().toArray());
        result = 31 * result + (isParallel() ? 1 : 0);
        return result;
    }

    public static <T> StandardHStream<T> empty() { return new StandardHStream<>(); }
    public static <T> StandardHStream<T> of(T... values) { return new StandardHStream<>(values); }
    public static <T> StandardHStream<T> of(Stream<T> value) { return new StandardHStream<>(value); }
    public static <T> StandardHStream<T> of(List<T> list) { return new StandardHStream<>((T) list.toArray()); }
    public static <T> StandardHStream<T> of(Enumeration<T> enumeration) {
        if (enumeration == null || !enumeration.hasMoreElements()) return StandardHStream.empty();
        T[] values = (T[]) new Object[] {enumeration.nextElement()};
        while (enumeration.hasMoreElements()) {
            values = Arrays.copyOf(values, values.length + 1);
            values[values.length - 1] = enumeration.nextElement();
        }

        return StandardHStream.of(values);
    }
}
