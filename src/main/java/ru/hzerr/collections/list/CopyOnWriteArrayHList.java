package ru.hzerr.collections.list;

import org.jetbrains.annotations.NotNull;
import ru.hzerr.stream.HStream;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * @version 1.5.3
 * @param <E> the type of elements held in this collection
 */
@SuppressWarnings("unchecked")
public final class CopyOnWriteArrayHList<E> extends CopyOnWriteArrayList<E> implements HList<E> {

    final transient ReentrantLock lock = new ReentrantLock();

    public CopyOnWriteArrayHList() { super(); }
    public CopyOnWriteArrayHList(E[] toCopyIn) { super(toCopyIn); }
    public CopyOnWriteArrayHList(Collection<? extends E> collection) { super(collection); }

    @Override
    public void changeIf(Predicate<? super E> condition, Consumer<? super E> changer) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (E element : this) {
                if (condition.test(element)) {
                    changer.accept(element);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean noContains(E element) {
        return !contains(element);
    }

    @Override
    public boolean noContains(Predicate<? super E> action) {
        return !contains(action);
    }

    @Override
    public boolean contains(Predicate<? super E> action) {
        for (E element : this) {
            if (action.test(element)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Optional<E> find(Predicate<? super E> predicate) {
        for (E element: this) {
            if (predicate.test(element)) return Optional.of(element);
        }

        return Optional.empty();
    }

    /**
     *
     * @param predicate search condition
     * @return no thread-safe collection
     */
    @Override
    public Collection<E> findAll(Predicate<? super E> predicate) {
        HList<E> values = new ArrayHList<>();
        for (E element: this) {
            if (predicate.test(element)) values.add(element);
        }

        return values;
    }

    // maybe check isLocked? Probably will be used in other methods
    @Override
    @SafeVarargs
    public final void addAll(E... elements) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (E element : elements) {
                add(element);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    @SafeVarargs
    public final void setAll(E... elements) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            clear();
            for (E element : elements) {
                //noinspection UseBulkOperation
                add(element);
            }
        } finally {
            lock.unlock();
        }
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
    public HStream<E> toHStream() {
        return HStream.of((E[]) this.toArray());
    }

    @Override
    public void replaceIf(Predicate<? super E> condition, E replacement) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (int i = 0; i < size(); i++) {
                E element = get(i);
                if (condition.test(element)) {
                    set(i, replacement);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void replaceIf(Predicate<? super E> condition, UnaryOperator<E> replacer) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (int i = 0; i < size(); i++) {
                E element = get(i);
                if (condition.test(element)) {
                    set(i, replacer.apply(element));
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return thread-safe collection
     */
    @Override
    public <R> HList<R> map(Function<? super E, ? extends R> mapper) {
        HList<R> list = new CopyOnWriteArrayHList<R>();
        for (E element : this) {
            list.add(mapper.apply(element));
        }

        return list;
    }

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
    public static <E> CopyOnWriteArrayHList<E> create(@NotNull E... elements) {
        return new CopyOnWriteArrayHList<>(Arrays.asList(elements));
    }
}
