package ru.hzerr.stream.function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents a predicate (boolean-valued function) of one argument.
 * This is a functional interface whose functional method is test(Object).
 * Since:
 * 1.8
 * Type parameters:
 * <T> – the type of the input to the predicate
 */
@FunctionalInterface
public interface Predicate<T> {

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param t the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     * @throws Exception if unable to compute a result
     */

    boolean test(T t) throws Exception;

    default Predicate<T> and(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) && other.test(t);
    }

    /**
     * Returns a predicate that represents the logical negation of this
     * predicate.
     *
     * @return a predicate that represents the logical negation of this
     * predicate
     */
    default Predicate<T> negate() {
        return (t) -> !test(t);
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical
     * OR of this predicate and another.  When evaluating the composed
     * predicate, if this predicate is {@code true}, then the {@code other}
     * predicate is not evaluated.
     *
     * <p>Any exceptions thrown during evaluation of either predicate are relayed
     * to the caller; if evaluation of this predicate throws an exception, the
     * {@code other} predicate will not be evaluated.
     *
     * @param other a predicate that will be logically-ORed with this
     *              predicate
     * @return a composed predicate that represents the short-circuiting logical
     * OR of this predicate and the {@code other} predicate
     * @throws NullPointerException if other is null
     */
    default Predicate<T> or(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) || other.test(t);
    }

    /**
     * Returns a predicate that tests if two arguments are equal according
     * to {@link Objects#equals(Object, Object)}.
     *
     * @param <T> the type of arguments to the predicate
     * @param targetRef the object reference with which to compare for equality,
     *               which may be {@code null}
     * @return a predicate that tests if two arguments are equal according
     * to {@link Objects#equals(Object, Object)}
     */
    static <T> Predicate<T> isEqual(Object targetRef) {
        return (null == targetRef)
                ? Objects::isNull
                : targetRef::equals;
    }

    /**
     * Converts class {@link Predicate} to class {@link java.util.function.Predicate}
     * @param predicate the object you want to convert
     * @param catchFunc action, in case of failure
     * @param defaultValue variable returned in case of failure by default
     * @param <T> the type of arguments to the predicate
     * @return new {@link java.util.function.Predicate<T>}
     */
    static <T> java.util.function.Predicate<T> convert(@NotNull Predicate<T> predicate,
                                                       @Nullable Consumer<Exception> catchFunc,
                                                       @Nullable Boolean defaultValue) {
        return t -> {
            try {
                return predicate.test(t);
            } catch (Exception e) {
                if (Objects.nonNull(catchFunc)) {
                    catchFunc.accept(e);
                }
            }
            return Objects.nonNull(defaultValue) ? defaultValue : false;
        };
    }

    static <T> java.util.function.Predicate<T> convert(@NotNull Predicate<T> predicate,
                                                       @Nullable Consumer<Exception> catchFunc) {
        return t -> {
            try {
                return predicate.test(t);
            } catch (Exception e) {
                if (Objects.nonNull(catchFunc)) {
                    catchFunc.accept(e);
                }
                return false;
            }
        };
    }

    static <T> java.util.function.Predicate<T> convert(@NotNull Predicate<T> predicate) {
        return t -> {
            try {
                return predicate.test(t);
            } catch (Exception ignored) { return false; }
        };
    }
}
