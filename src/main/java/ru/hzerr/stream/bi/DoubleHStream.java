package ru.hzerr.stream.bi;

import ru.hzerr.stream.HStream;
import ru.hzerr.stream.function.BinaryOperator;
import ru.hzerr.stream.function.Consumer;
import ru.hzerr.stream.function.Predicate;

import java.util.Comparator;
import java.util.Optional;
import java.util.Random;

public class DoubleHStream<T1, T2> implements DoubleBaseHStream<T1, T2, DoubleHStream<T1, T2>> {

    private final HStream<T1> stream;
    private final HStream<T2> stream2;
    private final Class<T1> t1;
    private final Class<T2> t2;

    DoubleHStream(Class<T1> t1, Class<T2> t2, HStream<T1> stream1, HStream<T2> stream2) {
        this.stream = stream1;
        this.stream2 = stream2;
        this.t1 = t1;
        this.t2 = t2;
    }

    public <R> DoubleHStream<T1, T2> filter(Class<? extends R> clazz, Predicate<? super R> action) {
        boolean filtered = false;
        if (clazz.isAssignableFrom(t1)) {
            stream.filter((Predicate<? super T1>) action);
            filtered = true;
        }

        if (clazz.isAssignableFrom(t2)) {
            stream2.filter((Predicate<? super T2>) action);
            filtered = true;
        }
        if (filtered) return this;
        throw illegalArgumentException("predicate");
    }

    public <R> DoubleHStream<T1, T2> forEach(Class<? extends R> clazz, Consumer<? super R> action) {
        boolean applied = false;
        if (clazz.isAssignableFrom(t1)) {
            stream.forEach((Consumer<? super T1>) action);
            applied = true;
        }

        if (clazz.isAssignableFrom(t2)) {
            stream2.forEach((Consumer<? super T2>) action);
            applied = true;
        }
        if (applied) return this;
        throw illegalArgumentException("consumer");
    }

    @Override
    public <R> DoubleHStream<T1, T2> forEachOrdered(Class<? extends R> clazz, Consumer<? super R> action) {
        boolean applied = false;
        if (clazz.isAssignableFrom(t1)) {
            stream.forEachOrdered((Consumer<? super T1>) action);
            applied = true;
        }

        if (clazz.isAssignableFrom(t2)) {
            stream2.forEachOrdered((Consumer<? super T2>) action);
            applied = true;
        }
        if (applied) return this;
        throw illegalArgumentException("consumer");
    }

    @Override
    public <R> DoubleHStream<T1, T2> peek(Class<? extends R> clazz, Consumer<? super R> action) {
        boolean applied = false;
        if (clazz.isAssignableFrom(t1)) {
            stream.peek((Consumer<? super T1>) action);
            applied = true;
        }

        if (clazz.isAssignableFrom(t2)) {
            stream2.peek((Consumer<? super T2>) action);
            applied = true;
        }
        if (applied) return this;
        throw illegalArgumentException("consumer");
    }

    @Override
    public <R> boolean allMatch(Class<? extends R> clazz, Predicate<? super R> action) {
        if (clazz.isAssignableFrom(t1) && clazz.isAssignableFrom(t2)) {
            return stream.allMatch((Predicate<? super T1>) action) && stream2.allMatch((Predicate<? super T2>) action);
        }

        if (clazz.isAssignableFrom(t1)) {
            return stream.allMatch((Predicate<? super T1>) action);
        }

        if (clazz.isAssignableFrom(t2)) {
            return stream2.allMatch((Predicate<? super T2>) action);
        }
        throw illegalArgumentException("predicate");
    }

    @Override
    public <R> boolean anyMatch(Class<? extends R> clazz, Predicate<? super R> action) {
        if (clazz.isAssignableFrom(t1) && clazz.isAssignableFrom(t2)) {
            return stream.anyMatch((Predicate<? super T1>) action) && stream2.anyMatch((Predicate<? super T2>) action);
        }

        if (clazz.isAssignableFrom(t1)) {
            return stream.anyMatch((Predicate<? super T1>) action);
        }

        if (clazz.isAssignableFrom(t2)) {
            return stream2.anyMatch((Predicate<? super T2>) action);
        }
        throw illegalArgumentException("predicate");
    }

    @Override
    public <R> boolean noneMatch(Class<? extends R> clazz, Predicate<? super R> action) {
        if (clazz.isAssignableFrom(t1) && clazz.isAssignableFrom(t2)) {
            return stream.noneMatch((Predicate<? super T1>) action) && stream2.noneMatch((Predicate<? super T2>) action);
        }

        if (clazz.isAssignableFrom(t1)) {
            return stream.noneMatch((Predicate<? super T1>) action);
        }

        if (clazz.isAssignableFrom(t2)) {
            return stream2.noneMatch((Predicate<? super T2>) action);
        }
        throw illegalArgumentException("predicate");
    }

    @Override
    public <R> DoubleHStream<T1, T2> parallel(Class<? extends R> clazz) {
        boolean parallel = false;
        if (clazz.isAssignableFrom(t1)) {
            stream.parallel();
            parallel = true;
        }

        if (clazz.isAssignableFrom(t2)) {
            stream2.parallel();
            parallel = true;
        }
        if (parallel) return this;
        throw illegalArgumentException("class");
    }

    @Override
    public <R> boolean isParallel(Class<? extends R> clazz) {
        if (clazz.isAssignableFrom(t1) && clazz.isAssignableFrom(t2)) {
            return stream.isParallel() && stream2.isParallel();
        }

        if (clazz.isAssignableFrom(t1)) {
            return stream.isParallel();
        }

        if (clazz.isAssignableFrom(t2)) {
            return stream2.isParallel();
        }
        throw illegalArgumentException("class");
    }

    @Override
    public <R> Optional<R> findFirst(Class<? extends R> clazz) {
        if (clazz.isAssignableFrom(t1) && clazz.isAssignableFrom(t2)) {
            return (Optional<R>) stream.findFirst();
        }

        if (clazz.isAssignableFrom(t1)) {
            return (Optional<R>) stream.findFirst();
        }

        if (clazz.isAssignableFrom(t2)) {
            return (Optional<R>) stream2.findFirst();
        }
        throw illegalArgumentException("class");
    }

    @Override
    public <R> Optional<R> findAny(Class<? extends R> clazz) {
        if (clazz.isAssignableFrom(t1) && clazz.isAssignableFrom(t2)) {
            int r = new Random().nextInt(2);
            if (r == 0)
                return (Optional<R>) stream.findAny();
            else
                return (Optional<R>) stream2.findAny();
        }

        if (clazz.isAssignableFrom(t1)) {
            return (Optional<R>) stream.findAny();
        }

        if (clazz.isAssignableFrom(t2)) {
            return (Optional<R>) stream2.findAny();
        }
        throw illegalArgumentException("class");
    }

    @Override
    public <R> Optional<R> reduce(Class<? extends R> clazz, BinaryOperator<R> accumulator) {
        if (clazz.isAssignableFrom(t1) && clazz.isAssignableFrom(t2)) {
            Optional<R> opt1 = (Optional<R>) stream.reduce((BinaryOperator<T1>) accumulator);
            Optional<R> opt2 = (Optional<R>) stream2.reduce((BinaryOperator<T2>) accumulator);
            if (opt1.isPresent() && opt2.isPresent()) {
                try {
                    return Optional.of(accumulator.apply(opt1.get(), opt2.get()));
                } catch (Exception e) { throw new InternalError(e); }
            }

            if (opt1.isPresent()) return opt1;
            return opt2;
        }

        if (clazz.isAssignableFrom(t1)) {
            return (Optional<R>) stream.reduce((BinaryOperator<T1>) accumulator);
        }

        if (clazz.isAssignableFrom(t2)) {
            return (Optional<R>) stream2.reduce((BinaryOperator<T2>) accumulator);
        }
        throw illegalArgumentException("accumulator");
    }

    @Override
    public <R> Optional<R> min(Class<? extends R> clazz, Comparator<? super R> comparator) {
        return reduce(clazz, BinaryOperator.minBy(comparator));
    }

    @Override
    public <R> Optional<R> max(Class<? extends R> clazz, Comparator<? super R> comparator) {
        return reduce(clazz, BinaryOperator.maxBy(comparator));
    }

    @Override
    public <R> DoubleHStream<T1, T2> distinct(Class<? extends R> clazz) {
        boolean applied = false;
        if (clazz.isAssignableFrom(t1)) {
            stream.distinct();
            applied = true;
        }

        if (clazz.isAssignableFrom(t2)) {
            stream2.distinct();
            applied = true;
        }
        if (applied) return this;
        throw illegalArgumentException("class");
    }

    @Override
    public <R> DoubleHStream<T1, T2> sorted(Class<? extends R> clazz) {
        boolean sorted = false;
        if (clazz.isAssignableFrom(t1)) {
            stream.sorted();
            sorted = true;
        }

        if (clazz.isAssignableFrom(t2)) {
            stream2.sorted();
            sorted = true;
        }
        if (sorted) return this;
        throw illegalArgumentException("class");
    }

    @Override
    public <R> DoubleHStream<T1, T2> sorted(Class<? extends R> clazz, Comparator<? super R> comparator) {
        boolean sorted = false;
        if (clazz.isAssignableFrom(t1)) {
            stream.sorted((Comparator<? super T1>) comparator);
            sorted = true;
        }

        if (clazz.isAssignableFrom(t2)) {
            stream2.sorted((Comparator<? super T2>) comparator);
            sorted = true;
        }
        if (sorted) return this;
        throw illegalArgumentException("comparator");
    }

    @Override
    public <R> DoubleHStream<T1, T2> unordered(Class<? extends R> clazz) {
        boolean unordered = false;
        if (clazz.isAssignableFrom(t1)) {
            stream.unordered();
            unordered = true;
        }

        if (clazz.isAssignableFrom(t2)) {
            stream2.unordered();
            unordered = true;
        }
        if (unordered) return this;
        throw illegalArgumentException("class");
    }

    @Override
    public <R> DoubleHStream<T1, T2> skip(Class<? extends R> clazz, long n) {
        boolean skipped = false;
        if (clazz.isAssignableFrom(t1)) {
            stream.skip(n);
            skipped = true;
        }

        if (clazz.isAssignableFrom(t2)) {
            stream2.skip(n);
            skipped = true;
        }
        if (skipped) return this;
        throw illegalArgumentException("class");
    }

    @Override
    public <R> DoubleHStream<T1, T2> limit(Class<? extends R> clazz, long maxSize) {
        boolean limited = false;
        if (clazz.isAssignableFrom(t1)) {
            stream.limit(maxSize);
            limited = true;
        }

        if (clazz.isAssignableFrom(t2)) {
            stream2.limit(maxSize);
            limited = true;
        }
        if (limited) return this;
        throw illegalArgumentException("class");
    }

    @Override
    public <R> long count(Class<? extends R> clazz) {
        if (clazz.isAssignableFrom(t1) && clazz.isAssignableFrom(t2)) {
            return stream.count() + stream2.count();
        }

        if (clazz.isAssignableFrom(t1)) {
            return stream.count();
        }

        if (clazz.isAssignableFrom(t2)) {
            return stream2.count();
        }
        throw illegalArgumentException("class");
    }

    public <R> HStream<R> getHStream(Class<? extends R> clazz) {
        if (clazz.isAssignableFrom(t1) && clazz.isAssignableFrom(t2)) {
            throw new UnsupportedOperationException(clazz.getName() + " can't be applied to two HStream objects");
        }

        if (clazz.isAssignableFrom(t1)) {
            return (HStream<R>) stream;
        }

        if (clazz.isAssignableFrom(t2)) {
            return (HStream<R>) stream2;
        }

        throw illegalArgumentException("class");
    }

    private IllegalArgumentException illegalArgumentException(String arg) {
        StringBuilder message = new StringBuilder();
        message.append("The passed ").append(arg).append(" cannot be applied to internal HStream classes,");
        message.append(" because their generic parameters do not match");
        return new IllegalArgumentException(new ClassCastException(message.toString()));
    }
}
