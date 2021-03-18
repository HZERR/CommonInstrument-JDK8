package ru.hzerr.collections;

import ru.hzerr.collections.list.ArrayHList;
import ru.hzerr.collections.list.HList;
import ru.hzerr.collections.map.HMap;
import ru.hzerr.collections.map.HashHMap;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

@SuppressWarnings("unused")
public class HCollectors {

    private static final Set<Collector.Characteristics> CH_ID = Collections.unmodifiableSet(EnumSet.of(IDENTITY_FINISH));

    private HCollectors() {}

    public static <T> Collector<T, ?, HList<T>> toHList() {
        BinaryOperator<HList<T>> operator = (left, right) -> { left.addAll(right); return left; };
        return new CollectorImpl<>(ArrayHList::new, HList::add, operator, CH_ID);
    }

    public static <T, K, U>
    Collector<T, ?, HMap<K,U>> toHMap(Function<? super T, ? extends K> keyMapper,
                                    Function<? super T, ? extends U> valueMapper) {
        return new CollectorImpl<>(HashHMap::new,
                uniqKeysMapAccumulator(keyMapper, valueMapper),
                uniqKeysMapMerger(),
                CH_ID);
    }

    public static <T, K, U, M extends HMap<K, U>>
    Collector<T, ?, M> toHMap(Function<? super T, ? extends K> keyMapper,
                             Function<? super T, ? extends U> valueMapper,
                             BinaryOperator<U> mergeFunction,
                             Supplier<M> mapFactory) {
        BiConsumer<M, T> accumulator = (map, element) -> map.merge(keyMapper.apply(element), valueMapper.apply(element), mergeFunction);
        return new CollectorImpl<>(mapFactory, accumulator, mapMerger(mergeFunction), CH_ID);
    }

    private static <K, V, M extends HMap<K,V>>
    BinaryOperator<M> uniqKeysMapMerger() {
        return (m1, m2) -> {
            for (Map.Entry<K,V> e : m2.entrySet()) {
                K k = e.getKey();
                V v = Objects.requireNonNull(e.getValue());
                V u = m1.putIfAbsent(k, v);
                if (u != null) {
                    String message = String.format("Duplicate key %s (attempted merging values %s and %s)", k, u, v);
                    throw new IllegalStateException(message);
                }
            }
            return m1;
        };
    }

    private static <T, K, V>
    BiConsumer<HMap<K, V>, T> uniqKeysMapAccumulator(Function<? super T, ? extends K> keyMapper,
                                                    Function<? super T, ? extends V> valueMapper) {
        return (map, element) -> {
            K k = keyMapper.apply(element);
            V v = Objects.requireNonNull(valueMapper.apply(element));
            V u = map.putIfAbsent(k, v);
            if (u != null) {
                String message = String.format("Duplicate key %s (attempted merging values %s and %s)", k, u, v);
                throw new IllegalStateException(message);
            }
        };
    }

    private static <K, V, M extends HMap<K, V>>
    BinaryOperator<M> mapMerger(BinaryOperator<V> mergeFunction) {
        return (m1, m2) -> {
            for (Map.Entry<K, V> e : m2.entrySet())
                m1.merge(e.getKey(), e.getValue(), mergeFunction);
            return m1;
        };
    }

    static class CollectorImpl<T, A, R> implements Collector<T, A, R> {

        private final Supplier<A> supplier;
        private final BiConsumer<A, T> accumulator;
        private final BinaryOperator<A> combiner;
        private final Function<A, R> finisher;
        private final Set<Characteristics> characteristics;

        CollectorImpl(Supplier<A> supplier,
                      BiConsumer<A, T> accumulator,
                      BinaryOperator<A> combiner,
                      Function<A,R> finisher,
                      Set<Characteristics> characteristics) {
            this.supplier = supplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
            this.finisher = finisher;
            this.characteristics = characteristics;
        }

        CollectorImpl(Supplier<A> supplier,
                      BiConsumer<A, T> accumulator,
                      BinaryOperator<A> combiner,
                      Set<Characteristics> characteristics) {
            this(supplier, accumulator, combiner, castingIdentity(), characteristics);
        }

        @Override
        public BiConsumer<A, T> accumulator() {
            return accumulator;
        }

        @Override
        public Supplier<A> supplier() {
            return supplier;
        }

        @Override
        public BinaryOperator<A> combiner() {
            return combiner;
        }

        @Override
        public Function<A, R> finisher() {
            return finisher;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return characteristics;
        }
    }

    private static <I, R> Function<I, R> castingIdentity() {
        //noinspection unchecked
        return i -> (R) i;
    }
}
