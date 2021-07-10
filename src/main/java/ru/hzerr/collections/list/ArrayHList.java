package ru.hzerr.collections.list;

import org.jetbrains.annotations.NotNull;
import ru.hzerr.stream.HStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class ArrayHList<E> extends ArrayList<E> implements HList<E> {

    public ArrayHList() { super(); }
    public ArrayHList(int initialCapacity) { super(initialCapacity); }
    public ArrayHList(Collection<? extends E> collection) { super(collection); }

    @Override
    public <R> HList<R> map(Function<? super E, ? extends R> mapper) {
        //noinspection unchecked
        E[] elements = (E[]) toArray();
        HList<R> list = new ArrayHList<>();
        for (E element : elements) {
            list.add(mapper.apply(element));
        }

        return list;
    }

    @Override
    public void changeIf(UnaryOperator<E> changer, Predicate<E> condition) {
        for (int i = 0; i < size(); i++) {
            E element = get(i);
            if (condition.test(element)) {
                this.set(i, changer.apply(element));
            }
        }
    }

    @Override
    public Optional<E> find(Predicate<E> predicate) {
        for (E element: this) {
            if (predicate.test(element)) return Optional.of(element);
        }

        return Optional.empty();
    }

    @Override
    public HList<E> findAll(Predicate<E> predicate) {
        HList<E> values = new ArrayHList<>();
        for (E element: this) {
            if (predicate.test(element)) values.add(element);
        }

        return values;
    }

    @Override
    public boolean noContains(E element) { return !contains(element); }

    @Override
    public boolean noContains(Predicate<E> action) { return !contains(action); }

    @Override
    public boolean contains(Predicate<E> predicate) {
        //noinspection unchecked
        for (E element : (E[]) this.toArray()) {
            if (predicate.test(element)) {
                return true;
            }
        }

        return false;
    }

    @Override
    @SafeVarargs
    @SuppressWarnings("ManualArrayToCollectionCopy")
    public final void add(E... elements) {
        for (E element : elements) {
            super.add(element);
        }
    }

    @Override
    @SafeVarargs
    public final void setAll(E... elements) {
        clear();
        add(elements);
    }

    @Override
    @SuppressWarnings("unchecked")
    public HStream<E> toHStream() { return HStream.of((E[]) this.toArray()); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof HList) {
            //noinspection unchecked
            HList<E> target = (HList<E>) o;
            return this.containsAll(target) && target.containsAll(this);
        }

        return false;
    }

    @SafeVarargs
    public static <E> ArrayHList<E> create(@NotNull E... elements) {
        return new ArrayHList<>(Arrays.asList(elements));
    }
}
