package ru.hzerr.stream;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import ru.hzerr.stream.function.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * @author HZERR
 * @see Stream
 */

@SuppressWarnings({"unchecked", "unused", "MethodDoesntCallSuperMethod"})
public class HStream<T> implements BaseHStream<T, HStream<T>>, Functions<T>, Cloneable {

    private HStream() {
        this.value = Stream::empty;
    }
    private HStream(T... values) {
        this.value = () -> Stream.of(values);
    }
    private HStream(Stream<T> stream) {
        this.value = () -> stream;
    }
    private HStream(Supplier<Stream<T>> value, java.util.function.Consumer<Exception> catchFunc, boolean isParallel) {
        this.value = value;
        this.catchFunc = catchFunc;
        if (isParallel) {
            parallel();
        }
    }

    protected Supplier<Stream<T>> value;
    protected java.util.function.Consumer<Exception> catchFunc;

    public HStream<T> wrap(java.util.function.Consumer<Exception> catchFunc) {
        this.catchFunc = catchFunc;
        return this;
    }

    @Override
    public HStream<T> filter(Predicate<? super T> action) {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().filter(Predicate.convert(action, catchFunc));
        return this;
    }

    @Override
    public HStream<T> peek(Consumer<? super T> action) {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().peek(Consumer.convert(action));
        return this;
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return this.value.get().reduce(identity, BinaryOperator.convert(accumulator, catchFunc));
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return this.value.get().reduce(BinaryOperator.convert(accumulator));
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return this.value.get()
                .reduce(identity, BiFunction.convert(accumulator, catchFunc), BinaryOperator.convert(combiner, catchFunc));
    }

    @Override
    public boolean allMatch(Predicate<? super T> action) {
        return this.value.get().allMatch(Predicate.convert(action, catchFunc));
    }

    @Override
    public boolean anyMatch(Predicate<? super T> action) {
        return this.value.get().anyMatch(Predicate.convert(action, catchFunc));
    }

    @Override
    public boolean noneMatch(Predicate<? super T> action) {
        return this.value.get().noneMatch(Predicate.convert(action, catchFunc));
    }

    @Override
    public HStream<T> forEach(Consumer<? super T> action) {
        this.value.get().forEach(Consumer.convert(action, catchFunc));
        return this;
    }

    @Override
    public HStream<T> forEachOrdered(Consumer<? super T> action) {
        this.value.get().forEachOrdered(Consumer.convert(action, catchFunc));
        return this;
    }

    @Override
    public Optional<T> findFirst() {
        return this.value.get().findFirst();
    }

    @Override
    public Optional<T> findAny() {
        return this.value.get().findAny();
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return this.value.get().collect(collector);
    }

    @Override
    public <R> R collect(ru.hzerr.stream.function.Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return this.value.get().collect(
                ru.hzerr.stream.function.Supplier.convert(supplier),
                BiConsumer.convert(accumulator),
                BiConsumer.convert(combiner));
    }

    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return this.value.get().min(comparator);
    }

    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        return this.value.get().max(comparator);
    }

    @Override
    public HStream<T> distinct() {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().distinct();
        return this;
    }

    @Override
    public HStream<T> sorted() {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().sorted();
        return this;
    }

    @Override
    public HStream<T> sorted(Comparator<? super T> comparator) {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().sorted(comparator);
        return this;
    }

    @Override
    public HStream<T> unordered() {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().unordered();
        return this;
    }

    @Override
    public HStream<T> skip(long n) {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().skip(n);
        return this;
    }

    @Override
    public HStream<T> limit(long maxSize) {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().limit(maxSize);
        return this;
    }

    @Override
    public long count() {
        return this.value.get().count();
    }

    @Override
    public HStream<T> parallel() {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().parallel();
        return this;
    }

    @Override
    public boolean isParallel() {
        return this.value.get().isParallel();
    }

    @Override
    public <R> HStream<R> map(Function<? super T, ? extends R> mapper) {
        Supplier<Stream<T>> current = this.value;
        return new HStream<>(current.get().map(Function.convert(mapper)));
    }

    /**
     * Adds 1 item to the stream by creating a new stream
     * The operation is slow and not recommended for frequent use
     * @param t element
     * @return modified instance of HStream
     */
    public HStream<T> put(T t) {
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
     * Performs the action specified by the condition for each group of elements
     *
     * @param condition            condition that divides the elements into two groups
     * @param actionForElementsYes action, for elements satisfying the condition
     * @param actionForElementsNo  action, for elements that do not satisfy the condition
     */
    @Override
    public void biForEach(Predicate<? super T> condition,
                          Consumer<? super T> actionForElementsYes,
                          Consumer<? super T> actionForElementsNo) {
        this.forEach((t) -> {
            if (Predicate.convert(condition, catchFunc).test(t)) Consumer.convert(actionForElementsYes).accept(t);
            else Consumer.convert(actionForElementsNo).accept(t);
        });
    }

    /**
     * Returns a stream consisting of the results of applying the given functions to the elements of this stream
     *
     * @param condition            condition that divides the elements into two groups
     * @param actionForElementsYes action, for elements satisfying the condition
     * @param actionForElementsNo  action, for elements that do not satisfy the condition
     * @param <R>                  The element type of the new stream
     * @return the new stream
     */
    @Override
    public <R> HStream<R> biMap(Predicate<? super T> condition,
                                Function<? super T, ? extends R> actionForElementsYes,
                                Function<? super T, ? extends R> actionForElementsNo) {
        return this.map(element -> {
            if (Predicate.convert(condition, catchFunc).test(element)) return actionForElementsYes.apply(element);
            return actionForElementsNo.apply(element);
        });
    }

    /**
     * Clones the object, creating a new thread identical to the instance
     * @return a clone of this instance
     */
    @Override
    public HStream<T> clone() {
        return new HStream<>(value, catchFunc, isParallel());
    }

    /**
     * Compares the contents of the threads.
     * Parallelism and the {@linkplain #catchFunc} function do not affect the result
     * @param o HStream object for comparison
     * @return true if this object is the same as the o argument; false otherwise
     */
    @Override
    public boolean contentEquals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        HStream<?> hStream = (HStream<?>) o;

        return new EqualsBuilder().append(value, hStream.value).isEquals();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        HStream<?> hStream = (HStream<?>) o;

        return new EqualsBuilder()
                .append(value, hStream.value)
                .append(isParallel(), hStream.isParallel())
                .append(catchFunc, hStream.catchFunc).isEquals();
    }

    /**
     * Returns a hash code value for the object.
     * Parallelism and the {@linkplain #catchFunc} function do not affect the value of the hash code
     * @return hash code of the instance
     */
    @Override
    public int contentHashCode() { return new HashCodeBuilder(17, 37).append(value).toHashCode(); }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(value)
                .append(isParallel())
                .append(catchFunc).toHashCode();
    }

    public static <T> HStream<T> empty() {
        return new HStream<>();
    }
    public static <T> HStream<T> of(T... values) {
        return new HStream<>(values);
    }
    public static <T> HStream<T> of(Stream<T> value) {
        return new HStream<>(value);
    }
    public static <T> HStream<T> of(List<T> list) {
        return new HStream<>((T) list.toArray());
    }
    public static <T> HStream<T> of(Enumeration<T> enumeration) {
        if (enumeration == null || !enumeration.hasMoreElements()) return HStream.empty();
        T[] values = (T[]) new Object[]{enumeration.nextElement()};
        while (enumeration.hasMoreElements()) {
            values = Arrays.copyOf(values, values.length + 1);
            values[values.length - 1] = enumeration.nextElement();
        }

        return HStream.of(values);
    }
}
