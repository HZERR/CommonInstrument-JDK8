package ru.hzerr.stream.standard;

import ru.hzerr.stream.Receiver;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    private StandardHStream(MapBoxer<?, T> mapBoxer) { this.value = mapBoxer::apply; }
    private StandardHStream(T seed, UnaryOperator<T> f) { this.value = () -> Stream.iterate(seed, f); }
    private StandardHStream(StandardHStream<? extends T> a, StandardHStream<? extends T> b) {
        this.value = () -> StreamSupport.stream(new ConcatSpliterator.OfRef<>((Spliterator<T>) a.spliterator(), (Spliterator<T>) b.spliterator()), a.isParallel() || b.isParallel());
    }
    private StandardHStream(Supplier<Stream<T>> value, boolean isParallel) {
        this.value = value;
        if (isParallel) {
            parallel();
        }
    }

    private Supplier<Stream<T>> value;

    @Override
    public Iterator<T> iterator() { return this.value.get().iterator(); }

    @Override
    public Spliterator<T> spliterator() { return this.value.get().spliterator(); }

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
    public StandardHStream<T> sequential() {
        Supplier<Stream<T>> current = this.value;
        this.value = () -> current.get().sequential();
        return this;
    }

    @Override
    public void forEach(Consumer<? super T> action) { this.value.get().forEach(action); }

    @Override
    public void forEachOrdered(Consumer<? super T> action) { this.value.get().forEachOrdered(action); }

    @Override
    public <R> StandardHStream<R> map(Function<? super T, ? extends R> mapper) {
        Supplier<Stream<T>> current = this.value;
        return new StandardHStream<>(MapBoxer.create(current, mapper));
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
        boolean parallelIdentity = hStream.isParallel() == isParallel();
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
    public static <T> StandardHStream<T> of(Supplier<Stream<T>> stream, boolean isParallel) { return new StandardHStream<>(stream, isParallel); }
    public static <T> StandardHStream<T> of(Supplier<Stream<T>> stream) { return new StandardHStream<>(stream, false); }
    public static <T> StandardHStream<T> of(List<T> list) { return new StandardHStream<>((T) list.toArray()); }
    public static <T> StandardHStream<T> of(Enumeration<T> enumeration) {
        List<T> values = Collections.list(enumeration);
        return of((T[]) values.toArray());
    }
    public static <T> StandardHStream<T> of(Receiver<Stream<T>> streamReceiver, boolean isParallel) { return new StandardHStream<>(streamReceiver.asSupplier(), isParallel); }
    public static <T> StandardHStream<T> of(Receiver<Stream<T>> streamReceiver) { return new StandardHStream<>(streamReceiver.asSupplier(), false); }
    public static <T> StandardHStream<T> of(Receiver.Void<Stream<T>> streamReceiver, boolean isParallel) { return new StandardHStream<>(streamReceiver.asSupplier(), isParallel); }
    public static <T> StandardHStream<T> of(Receiver.Void<Stream<T>> streamReceiver) { return new StandardHStream<>(streamReceiver.asSupplier(), false); }
    public static <T> StandardHStream<T> iterate(final T seed, final UnaryOperator<T> f) { return new StandardHStream<>(seed, f); }
    public static <T> StandardHStream<T> concat(StandardHStream<? extends T> a, StandardHStream<? extends T> b) { return new StandardHStream<>(a, b); }

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

        static class OfRef<T> extends StandardHStream.ConcatSpliterator<T, Spliterator<T>> {
            OfRef(Spliterator<T> aSpliterator, Spliterator<T> bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }

        private abstract static class OfPrimitive<T, T_CONS, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>>
                extends StandardHStream.ConcatSpliterator<T, T_SPLITR>
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
                extends StandardHStream.ConcatSpliterator.OfPrimitive<Integer, IntConsumer, Spliterator.OfInt>
                implements Spliterator.OfInt {
            OfInt(Spliterator.OfInt aSpliterator, Spliterator.OfInt bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }

        static class OfLong
                extends StandardHStream.ConcatSpliterator.OfPrimitive<Long, LongConsumer, Spliterator.OfLong>
                implements Spliterator.OfLong {
            OfLong(Spliterator.OfLong aSpliterator, Spliterator.OfLong bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }

        static class OfDouble
                extends StandardHStream.ConcatSpliterator.OfPrimitive<Double, DoubleConsumer, Spliterator.OfDouble>
                implements Spliterator.OfDouble {
            OfDouble(Spliterator.OfDouble aSpliterator, Spliterator.OfDouble bSpliterator) {
                super(aSpliterator, bSpliterator);
            }
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static class MapBoxer<T, R> {

        private final Supplier<Stream<T>> container;
        private final Function<? super T, ? extends R> mapAction;

        private MapBoxer(Supplier<Stream<T>> container, Function<? super T, ? extends R> mapAction) {
            this.container = container;
            this.mapAction = mapAction;
        }

        public Stream<R> apply() { return container.get().map(mapAction); }

        public static <T, R> MapBoxer<T, R> create(Supplier<Stream<T>> container, Function<? super T, ? extends R> mapAction) {
            return new MapBoxer<>(container, mapAction);
        }
    }
}
