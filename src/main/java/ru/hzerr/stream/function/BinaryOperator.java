package ru.hzerr.stream.function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an operation upon two operands of the same type, producing a result
 * of the same type as the operands.  This is a specialization of
 * {@link java.util.function.BiFunction} for the case where the operands and the result are all of
 * the same type.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object, Object)}.
 *
 * @param <T> the type of the operands and result of the operator
 * @see BiFunction
 * @since 1.8
 */
public interface BinaryOperator<T> extends BiFunction<T, T, T> {

    /**
     * Returns a {@link BinaryOperator} which returns the lesser of two elements
     * according to the specified {@code Comparator}.
     *
     * @param <T>        the type of the input arguments of the comparator
     * @param comparator a {@code Comparator} for comparing the two values
     * @return a {@code BinaryOperator} which returns the lesser of its operands,
     * according to the supplied {@code Comparator}
     * @throws NullPointerException if the argument is null
     */
    static <T> BinaryOperator<T> minBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) <= 0 ? a : b;
    }

    /**
     * Returns a {@link BinaryOperator} which returns the greater of two elements
     * according to the specified {@code Comparator}.
     *
     * @param <T>        the type of the input arguments of the comparator
     * @param comparator a {@code Comparator} for comparing the two values
     * @return a {@code BinaryOperator} which returns the greater of its operands,
     * according to the supplied {@code Comparator}
     * @throws NullPointerException if the argument is null
     */
    static <T> BinaryOperator<T> maxBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return (a, b) -> comparator.compare(a, b) >= 0 ? a : b;
    }

    static <T> java.util.function.BinaryOperator<T> convert(@NotNull BinaryOperator<T> orig) {
        return (t, u) -> {
            try {
                return orig.apply(t, u);
            } catch (Exception ignored) {
                return null;
            }
        };
    }

    static <T> java.util.function.BinaryOperator<T> convert(@NotNull BinaryOperator<T> orig,
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

    static <T> java.util.function.BinaryOperator<T> convert(@NotNull BinaryOperator<T> orig,
                                                            @Nullable Consumer<Exception> catchFunc,
                                                            @Nullable T defaultValue) {
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
