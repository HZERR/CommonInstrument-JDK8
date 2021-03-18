package ru.hzerr.stream.function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents a function that accepts two arguments and produces a result.
 * This is the two-arity specialization of {@link Function}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object, Object)}.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <R> the type of the result of the function
 *
 * @see Function
 * @since 1.8
 */
@FunctionalInterface
public interface BiFunction<T, U, R> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @throws Exception if unable to compute a result
     * @return the function result
     */
    R apply(T t, U u) throws Exception;

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the {@code after} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V> the type of output of the {@code after} function, and of the
     *           composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     */
    default <V> BiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> after.apply(apply(t, u));
    }

    static <T, U, V> java.util.function.BiFunction<T, U, V> convert(@NotNull BiFunction<T, U, V> orig) {
        return (t, u) -> {
            try {
                return orig.apply(t, u);
            } catch (Exception ignored) { return null; }
        };
    }

    static <T, U, V> java.util.function.BiFunction<T, U, V> convert(@NotNull BiFunction<T, U, V> orig,
                                                                    @Nullable Consumer<Exception> catchFunc) {
        return (t, u) -> {
            try {
                return orig.apply(t, u);
            } catch (Exception e) {
                if (Objects.nonNull(catchFunc)) catchFunc.accept(e);
                return null;
            }
        };
    }

    static <T, U, V> java.util.function.BiFunction<T, U, V> convert(@NotNull BiFunction<T, U, V> orig,
                                                                    @Nullable Consumer<Exception> catchFunc,
                                                                    @Nullable V defaultValue) {
        return (t, u) -> {
            try {
                return orig.apply(t, u);
            } catch (Exception e) {
                if (Objects.nonNull(catchFunc)) catchFunc.accept(e);
                return Objects.nonNull(defaultValue) ? defaultValue : null;
            }
        };
    }
}