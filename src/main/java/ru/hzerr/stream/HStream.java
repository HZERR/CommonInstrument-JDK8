package ru.hzerr.stream;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import ru.hzerr.stream.function.BiConsumer;
import ru.hzerr.stream.function.BiFunction;
import ru.hzerr.stream.function.BinaryOperator;
import ru.hzerr.stream.function.Consumer;
import ru.hzerr.stream.function.Function;
import ru.hzerr.stream.function.Predicate;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Extended version of the {@link ru.hzerr.stream.standard.StandardHStream} class.<br/>
 * Если вы хотите создать класс из Stream, то используйте {@link #of(Receiver)}, {@link #of(Receiver.Void)} или {@link #of(Supplier)}
 * @author HZERR
 * @see Stream
 */
@SuppressWarnings({"unchecked", "unused", "MethodDoesntCallSuperMethod"})
public class HStream<T> implements BaseHStream<T, HStream<T>>, Functions<T>, Cloneable {

    private HStream() { this.value = Stream::empty; }
    private HStream(T... values) { this.value = () -> Stream.of(values); }
    private HStream(Stream<T> stream) { this.value = () -> stream; }
    private HStream(MapBoxer<?, T> mapBoxer) { this.value = mapBoxer::apply; }
    private HStream(Enumeration<T> e) {
        List<T> values = Collections.list(e);
        this.value = () -> Stream.of((T[]) values.toArray());
    }
    private HStream(Spliterator<T> spliterator, boolean isParallel) { this.value = () -> StreamSupport.stream(spliterator, isParallel); }
    private HStream(Iterable<T> iterable, boolean isParallel) { this.value = () -> StreamSupport.stream(iterable.spliterator(), isParallel); }
    private HStream(Iterator<T> iterator, boolean isParallel) { this.value = () -> StreamSupport.stream(((Iterable<T>) (() -> iterator)).spliterator(), isParallel); }
    private HStream(T seed, UnaryOperator<T> f) { this.value = () -> Stream.iterate(seed, f); }
    private HStream(T seed, java.util.function.Predicate<? super T> hasNext, UnaryOperator<T> next) { this.value = () -> Stream.iterate(seed, hasNext, next); }
    private HStream(HStream<? extends T> a, HStream<? extends T> b) {
        this.value = () -> StreamSupport.stream(new ConcatSpliterator.OfRef<>((Spliterator<T>) a.spliterator(), (Spliterator<T>) b.spliterator()), a.isParallel() || b.isParallel());
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
    public Iterator<T> iterator() { return this.value.get().iterator(); }

    @Override
    public Spliterator<T> spliterator() { return this.value.get().spliterator(); }

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
    public HStream<T> sequential() {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().sequential();
        return this;
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
    public long count() { return this.value.get().count(); }

    @Override
    public HStream<T> parallel() {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().parallel();
        return this;
    }

    @Override
    public HStream<T> parallelIfNeeded() {
        if (count() > 10_000) parallel();
        return this;
    }

    @Override
    public boolean isParallel() {
        return this.value.get().isParallel();
    }

    @Override
    public <R> HStream<R> map(Function<? super T, ? extends R> mapper) {
        Supplier<Stream<T>> current = this.value;
        return new HStream<>(MapBoxer.create(current, Function.convert(mapper, catchFunc)));
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


    @Override
    public HStream<T> biFilter(Predicate<? super T> condition, Predicate<? super T> actionForElementsYes, Predicate<? super T> actionForElementsNo) {
        this.filter(t -> condition.test(t) ? actionForElementsYes.test(t) : actionForElementsNo.test(t));
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
            if (Predicate.convert(condition, catchFunc).test(t)) {
                Consumer.convert(actionForElementsYes).accept(t);
            } else
                Consumer.convert(actionForElementsNo).accept(t);
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
        return this.map(element -> Predicate.convert(condition, catchFunc).test(element)
                ? actionForElementsYes.apply(element)
                : actionForElementsNo.apply(element));
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

        return new EqualsBuilder().append(value.get().toArray(), hStream.value.get().toArray()).isEquals();
    }

    /**
     * <pre>
     *     Example with return true
     *     {@code
     *     HStream<Integer> stream = HStream.of(1);
     *     HStream<Integer> stream2 = HStream.of(1);
     *     Consumer<Exception> wrapper = Throwable::printStackTrace;
     *     stream.wrap(wrapper);
     *     stream2.wrap(wrapper);
     *     System.out.println(stream.equals(stream2)); // return true
     *     }
     *     Example with return false
     *     {@code
     *     HStream<Integer> stream = HStream.of(1);
     *     HStream<Integer> stream2 = HStream.of(1);
     *     stream.wrap(Throwable::printStackTrace);
     *     stream2.wrap(Throwable::printStackTrace);
     *     System.out.println(stream.equals(stream2)); // return false
     *
     *     HStream<Integer> stream = HStream.of(1);
     *     HStream<Integer> stream2 = HStream.of(1);
     *     Consumer<Exception> wrapper = Throwable::printStackTrace;
     *     Consumer<Exception> wrapper2 = Throwable::printStackTrace;
     *     stream.wrap(wrapper);
     *     stream2.wrap(wrapper2);
     *     System.out.println(stream.equals(stream2)); // return false
     *     }
     * </pre>
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        HStream<?> hStream = (HStream<?>) o;
        return new EqualsBuilder()
                .append(value.get().toArray(), hStream.value.get().toArray())
                .append(isParallel(), hStream.isParallel())
                .append(catchFunc, hStream.catchFunc).isEquals();
    }

    /**
     * Returns a hash code value for the object.
     * Parallelism and the {@linkplain #catchFunc} function do not affect the value of the hash code
     * @return hash code of the instance
     */
    @Override
    public int contentHashCode() { return new HashCodeBuilder(17, 37).append(value.get().toArray()).toHashCode(); }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(value.get().toArray())
                .append(isParallel())
                .append(catchFunc).toHashCode();
    }

    public static <T> HStream<T> empty() { return new HStream<>(); }
    public static <T> HStream<T> of(T... values) { return new HStream<>(values); }
    public static <T> HStream<T> of(List<T> list) { return new HStream<>((T[]) list.toArray()); }
    public static <T> HStream<T> of(Enumeration<T> e) { return new HStream<>(e); }
    public static <T> HStream<T> of(Spliterator<T> sourceSpliterator, boolean isParallel) { return new HStream<>(sourceSpliterator, isParallel); }
    public static <T> HStream<T> of(Spliterator<T> sourceSpliterator) { return new HStream<>(sourceSpliterator, false); }
    public static <T> HStream<T> of(Iterable<T> sourceIterable, boolean isParallel) { return new HStream<>(sourceIterable, isParallel); }
    public static <T> HStream<T> of(Iterable<T> sourceIterable) { return new HStream<>(sourceIterable, false); }
    public static <T> HStream<T> of(Iterator<T> sourceIterator, boolean isParallel) { return new HStream<>(sourceIterator, isParallel); }
    public static <T> HStream<T> of(Iterator<T> sourceIterator) { return new HStream<>(sourceIterator, false); }
    public static <T> HStream<T> of(Supplier<Stream<T>> stream, boolean isParallel) { return new HStream<>(stream, null, isParallel); }
    public static <T> HStream<T> of(Supplier<Stream<T>> stream) { return new HStream<>(stream, null, false); }
    public static <T> HStream<T> of(Receiver<Stream<T>> streamReceiver, boolean isParallel) { return new HStream<>(Receiver.convert(streamReceiver), null, isParallel); }
    public static <T> HStream<T> of(Receiver<Stream<T>> streamReceiver) { return new HStream<>(Receiver.convert(streamReceiver), null, false); }
    public static <T> HStream<T> of(Receiver.Void<Stream<T>> streamReceiver, boolean isParallel) { return new HStream<>(Receiver.Void.convert(streamReceiver), null, isParallel); }
    public static <T> HStream<T> of(Receiver.Void<Stream<T>> streamReceiver) { return new HStream<>(Receiver.Void.convert(streamReceiver), null, false); }
    public static <T> HStream<T> iterate(final T seed, final UnaryOperator<T> f) { return new HStream<>(seed, f); }
    public static <T> HStream<T> iterate(T seed, java.util.function.Predicate<? super T> hasNext, UnaryOperator<T> next) { return new HStream<>(seed, hasNext, next); }
    public static <T> HStream<T> concat(HStream<? extends T> a, HStream<? extends T> b) { return new HStream<>(a, b); }

    abstract static class ConcatSpliterator<T, T_SPLITR extends Spliterator<T>> implements Spliterator<T> {
        protected final T_SPLITR aSpliterator;
        protected final T_SPLITR bSpliterator;
        // True when no split has occurred, otherwise false
        boolean beforeSplit;
        // Never read after splitting
        final boolean unsized;

        public ConcatSpliterator(T_SPLITR aSpliterator, T_SPLITR bSpliterator) {
            this.aSpliterator = aSpliterator;
            this.bSpliterator = bSpliterator;
            beforeSplit = true;
            // The spliterator is known to be unsized before splitting if the
            // sum of the estimates overflows.
            unsized = aSpliterator.estimateSize() + bSpliterator.estimateSize() < 0;
        }

        @Override
        public T_SPLITR trySplit() {
            @SuppressWarnings("unchecked")
            T_SPLITR ret = beforeSplit ? aSpliterator : (T_SPLITR) bSpliterator.trySplit();
            beforeSplit = false;
            return ret;
        }

        @Override
        public boolean tryAdvance(java.util.function.Consumer<? super T> consumer) {
            boolean hasNext;
            if (beforeSplit) {
                hasNext = aSpliterator.tryAdvance(consumer);
                if (!hasNext) {
                    beforeSplit = false;
                    hasNext = bSpliterator.tryAdvance(consumer);
                }
            }
            else
                hasNext = bSpliterator.tryAdvance(consumer);
            return hasNext;
        }

        @Override
        public void forEachRemaining(java.util.function.Consumer<? super T> consumer) {
            if (beforeSplit)
                aSpliterator.forEachRemaining(consumer);
            bSpliterator.forEachRemaining(consumer);
        }

        @Override
        public long estimateSize() {
            if (beforeSplit) {
                // If one or both estimates are Long.MAX_VALUE then the sum
                // will either be Long.MAX_VALUE or overflow to a negative value
                long size = aSpliterator.estimateSize() + bSpliterator.estimateSize();
                return (size >= 0) ? size : Long.MAX_VALUE;
            }
            else {
                return bSpliterator.estimateSize();
            }
        }

        @Override
        public int characteristics() {
            if (beforeSplit) {
                // Concatenation loses DISTINCT and SORTED characteristics
                return aSpliterator.characteristics() & bSpliterator.characteristics()
                        & ~(Spliterator.DISTINCT | Spliterator.SORTED
                        | (unsized ? Spliterator.SIZED | Spliterator.SUBSIZED : 0));
            }
            else {
                return bSpliterator.characteristics();
            }
        }

        @Override
        public Comparator<? super T> getComparator() {
            if (beforeSplit)
                throw new IllegalStateException();
            return bSpliterator.getComparator();
        }

        static class OfRef<T> extends ConcatSpliterator<T, Spliterator<T>> {
            OfRef(Spliterator<T> aSpliterator, Spliterator<T> bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }

        private abstract static class OfPrimitive<T, T_CONS, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>>
                extends ConcatSpliterator<T, T_SPLITR>
                implements Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {
            private OfPrimitive(T_SPLITR aSpliterator, T_SPLITR bSpliterator) {
                super(aSpliterator, bSpliterator);
            }

            @Override
            public boolean tryAdvance(T_CONS action) {
                boolean hasNext;
                if (beforeSplit) {
                    hasNext = aSpliterator.tryAdvance(action);
                    if (!hasNext) {
                        beforeSplit = false;
                        hasNext = bSpliterator.tryAdvance(action);
                    }
                }
                else
                    hasNext = bSpliterator.tryAdvance(action);
                return hasNext;
            }

            @Override
            public void forEachRemaining(T_CONS action) {
                if (beforeSplit)
                    aSpliterator.forEachRemaining(action);
                bSpliterator.forEachRemaining(action);
            }
        }

        static class OfInt
                extends ConcatSpliterator.OfPrimitive<Integer, IntConsumer, Spliterator.OfInt>
                implements Spliterator.OfInt {
            OfInt(Spliterator.OfInt aSpliterator, Spliterator.OfInt bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }

        static class OfLong
                extends ConcatSpliterator.OfPrimitive<Long, LongConsumer, Spliterator.OfLong>
                implements Spliterator.OfLong {
            OfLong(Spliterator.OfLong aSpliterator, Spliterator.OfLong bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }

        static class OfDouble
                extends ConcatSpliterator.OfPrimitive<Double, DoubleConsumer, Spliterator.OfDouble>
                implements Spliterator.OfDouble {
            OfDouble(Spliterator.OfDouble aSpliterator, Spliterator.OfDouble bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }
    }

    private static class MapBoxer<T, R> {

        private Supplier<Stream<T>> container;
        private java.util.function.Function<? super T, ? extends R> mapAction;

        private MapBoxer(Supplier<Stream<T>> container, java.util.function.Function<? super T, ? extends R> mapAction) {
            this.container = container;
            this.mapAction = mapAction;
        }

        public Stream<R> apply() { return container.get().map(mapAction); }

        public static <T, R> MapBoxer<T, R> create(Supplier<Stream<T>> container, java.util.function.Function<? super T, ? extends R> mapAction) {
            return new MapBoxer<>(container, mapAction);
        }
    }
}
