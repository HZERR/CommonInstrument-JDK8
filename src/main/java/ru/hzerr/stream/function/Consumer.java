package ru.hzerr.stream.function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface Consumer<T> {


    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(T t) throws Exception;

    /**
     * Returns a composed {@code Consumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code Consumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default Consumer<T> andThen(Consumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> { accept(t); after.accept(t); };
    }

    static <T> java.util.function.Consumer<T> convert(@NotNull Consumer<T> consumer,
                                                      @Nullable java.util.function.Consumer<Exception> catchFunc) {
        Objects.requireNonNull(consumer);
        return (T t) -> {
            try {
                consumer.accept(t);
            } catch (Exception e) {
                if (Objects.nonNull(catchFunc)) {
                    catchFunc.accept(e);
                }
            }
        };
    }

    static <T> java.util.function.Consumer<T> convert(@NotNull Consumer<T> consumer) {
        Objects.requireNonNull(consumer);
        return (T t) -> {
            try {
                consumer.accept(t);
            } catch (Exception ignored) {}
        };
    }
}
