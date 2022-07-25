package ru.hzerr.collections.list;

import org.jetbrains.annotations.NotNull;
import ru.hzerr.collections.functions.Functions;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.*;

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
    public <C extends Collection<E>> C to(Supplier<C> collectionFactory) {
        C collection = collectionFactory.get();
        collection.addAll(this);
        return collection;
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
        HList<R> list = new CopyOnWriteArrayHList<>();
        for (E element : this) {
            list.add(mapper.apply(element));
        }

        return list;
    }

    @Override
    public <TH extends Exception> boolean removeIf(Functions.Predicate<? super E, TH> filter, Class<TH> exception) throws TH {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            boolean removed = false;
            final Iterator<E> each = iterator();
            while (each.hasNext()) {
                if (filter.test(each.next())) {
                    each.remove();
                    removed = true;
                }
            }
            return removed;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <TH extends Exception, TH2 extends Exception> boolean removeIf(Functions.BiPredicate<? super E, TH, TH2> filter, Class<TH> exception, Class<TH2> exception2) throws TH, TH2 {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            boolean removed = false;
            final Iterator<E> each = iterator();
            while (each.hasNext()) {
                if (filter.test(each.next())) {
                    each.remove();
                    removed = true;
                }
            }
            return removed;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <TH extends Exception, TH2 extends Exception, TH3 extends Exception> boolean removeIf(Functions.ThPredicate<? super E, TH, TH2, TH3> filter, Class<TH> exception, Class<TH2> exception2, Class<TH3> exception3) throws TH, TH2, TH3 {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            boolean removed = false;
            final Iterator<E> each = iterator();
            while (each.hasNext()) {
                if (filter.test(each.next())) {
                    each.remove();
                    removed = true;
                }
            }
            return removed;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <R, TH extends Exception> HList<R> map(Functions.Func<? super E, ? extends R, TH> mapper, Class<TH> exception) throws TH {
        HList<R> list = new CopyOnWriteArrayHList<>();
        for (E element : this) {
            list.add(mapper.apply(element));
        }

        return list;
    }

    @Override
    public <R, TH extends Exception, TH2 extends Exception> HList<R> map(Functions.BiFunc<? super E, ? extends R, TH, TH2> mapper, Class<TH> exception, Class<TH2> exception2) throws TH, TH2 {
        HList<R> list = new CopyOnWriteArrayHList<>();
        for (E element : this) {
            list.add(mapper.apply(element));
        }

        return list;
    }

    @Override
    public <R, TH extends Exception, TH2 extends Exception, TH3 extends Exception> HList<R> map(Functions.ThFunc<? super E, ? extends R, TH, TH2, TH3> mapper, Class<TH> exception, Class<TH2> exception2, Class<TH3> exception3) throws TH, TH2, TH3 {
        HList<R> list = new CopyOnWriteArrayHList<>();
        for (E element : this) {
            list.add(mapper.apply(element));
        }

        return list;
    }

    @Override
    public boolean allMatch(Predicate<? super E> predicate) {
        for (E element : this) {
            if (!predicate.test(element)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public <TH extends Exception> boolean allMatch(Functions.Predicate<? super E, TH> predicate, Class<TH> exception) throws TH {
        for (E element : this) {
            if (!predicate.test(element)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public <TH extends Exception, TH2 extends Exception> boolean allMatch(Functions.BiPredicate<? super E, TH, TH2> predicate, Class<TH> exception, Class<TH2> exception2) throws TH, TH2 {
        for (E element : this) {
            if (!predicate.test(element)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public <TH extends Exception, TH2 extends Exception, TH3 extends Exception> boolean allMatch(Functions.ThPredicate<? super E, TH, TH2, TH3> predicate, Class<TH> exception, Class<TH2> exception2, Class<TH3> exception3) throws TH, TH2, TH3 {
        for (E element : this) {
            if (!predicate.test(element)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean anyMatch(Predicate<? super E> predicate) {
        for (E element : this) {
            if (predicate.test(element)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public <TH extends Exception> boolean anyMatch(Functions.Predicate<? super E, TH> predicate, Class<TH> exception) throws TH {
        for (E element : this) {
            if (predicate.test(element)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public <TH extends Exception, TH2 extends Exception> boolean anyMatch(Functions.BiPredicate<? super E, TH, TH2> predicate, Class<TH> exception, Class<TH2> exception2) throws TH, TH2 {
        for (E element : this) {
            if (predicate.test(element)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public <TH extends Exception, TH2 extends Exception, TH3 extends Exception> boolean anyMatch(Functions.ThPredicate<? super E, TH, TH2, TH3> predicate, Class<TH> exception, Class<TH2> exception2, Class<TH3> exception3) throws TH, TH2, TH3 {
        for (E element : this) {
            if (predicate.test(element)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean noneMatch(Predicate<? super E> predicate) {
        for (E element : this) {
            if (predicate.test(element)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public <TH extends Exception> boolean noneMatch(Functions.Predicate<? super E, TH> predicate, Class<TH> exception) throws TH {
        for (E element : this) {
            if (predicate.test(element)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public <TH extends Exception, TH2 extends Exception> boolean noneMatch(Functions.BiPredicate<? super E, TH, TH2> predicate, Class<TH> exception, Class<TH2> exception2) throws TH, TH2 {
        for (E element : this) {
            if (predicate.test(element)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public <TH extends Exception, TH2 extends Exception, TH3 extends Exception> boolean noneMatch(Functions.ThPredicate<? super E, TH, TH2, TH3> predicate, Class<TH> exception, Class<TH2> exception2, Class<TH3> exception3) throws TH, TH2, TH3 {
        for (E element : this) {
            if (predicate.test(element)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public <TH extends Exception> void forEach(Functions.Consumer<? super E, TH> action, Class<TH> exception) throws TH {
        for (E e : this) {
            action.accept(e);
        }
    }

    @Override
    public <TH extends Exception, TH2 extends Exception> void forEach(Functions.BiConsumer<? super E, TH, TH2> action, Class<TH> exception, Class<TH2> exception2) throws TH, TH2 {
        for (E e : this) {
            action.accept(e);
        }
    }

    @Override
    public <TH extends Exception, TH2 extends Exception, TH3 extends Exception> void forEach(Functions.ThConsumer<? super E, TH, TH2, TH3> action, Class<TH> exception, Class<TH2> exception2, Class<TH3> exception3) throws TH, TH2, TH3 {
        for (E e : this) {
            action.accept(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof HList) {
            //noinspection unchecked
            HList<E> target = (HList<E>) o;
            return new HashSet<>(this).containsAll(target) && new HashSet<>(target).containsAll(this);
        }

        return false;
    }

    @SafeVarargs
    public static <E> CopyOnWriteArrayHList<E> create(@NotNull E... elements) {
        return new CopyOnWriteArrayHList<>(Arrays.asList(elements));
    }
}
