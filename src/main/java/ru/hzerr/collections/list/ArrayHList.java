package ru.hzerr.collections.list;

import org.jetbrains.annotations.NotNull;
import ru.hzerr.stream.HStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public final class ArrayHList<E> extends ArrayList<E> implements HList<E> {

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
    public void changeIf(Predicate<? super E> condition, Consumer<? super E> changer) {
        for (E element : this) {
            if (condition.test(element)) {
                changer.accept(element);
            }
        }
    }

    @Override
    public void replaceIf(Predicate<? super E> condition, E replacement) {
        for (int i = 0; i < size(); i++) {
            E element = get(i);
            if (condition.test(element)) {
                set(i, replacement);
            }
        }
    }

    @Override
    public void replaceIf(Predicate<? super E> condition, UnaryOperator<E> replacer) {
        for (int i = 0; i < size(); i++) {
            E element = get(i);
            if (condition.test(element)) {
                set(i, replacer.apply(element));
            }
        }
    }

    @Override
    public Optional<E> find(Predicate<? super E> predicate) {
        for (E element: this) {
            if (predicate.test(element)) return Optional.of(element);
        }

        return Optional.empty();
    }

    @Override
    public HList<E> findAll(Predicate<? super E> predicate) {
        HList<E> values = new ArrayHList<>();
        for (E element: this) {
            if (predicate.test(element)) values.add(element);
        }

        return values;
    }

    @Override
    public boolean noContains(E element) { return !contains(element); }

    @Override
    public boolean noContains(Predicate<? super E> action) { return !contains(action); }

    @Override
    public boolean contains(Predicate<? super E> predicate) {
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
    public final void addAll(E... elements) {
        for (E element : elements) {
            super.add(element);
        }
    }

    @Override
    @SafeVarargs
    public final void setAll(E... elements) {
        clear();
        addAll(elements);
    }

    @Override
    public E firstElement() {
        return get(0);
    }

    @Override
    public E lastElement() {
        return get(size() - 1);
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
