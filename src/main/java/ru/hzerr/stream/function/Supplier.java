package ru.hzerr.stream.function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents a supplier of results.
 * There is no requirement that a new or distinct result be returned each time the supplier is invoked.
 * This is a functional interface whose functional method is get().
 * @since 1.8
 * @param <T> the type of results supplied by this supplier
 */
public interface Supplier<T> {


    /**
     * Gets a result.
     *
     * @return a result
     * @throws Exception if unable to compute a result
     */
    T get() throws Exception;

    static <T> java.util.function.Supplier<T> convert(@NotNull Supplier<T> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception ignored) { return null; }
        };
    }

    static <T> java.util.function.Supplier<T> convert(@NotNull Supplier<T> supplier,
                                                      @Nullable Consumer<Exception> catchFunc) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                if (Objects.nonNull(catchFunc)) catchFunc.accept(e);
                return null;
            }
        };
    }

    static <T> java.util.function.Supplier<T> convert(@NotNull Supplier<T> supplier,
                                                      @Nullable Consumer<Exception> catchFunc,
                                                      @Nullable T defaultValue) {
        return () -> {
            try {
                return supplier.get();
            } catch (Exception e) {
                if (Objects.nonNull(catchFunc)) catchFunc.accept(e);
                return Objects.nonNull(defaultValue) ? defaultValue : null;
            }
        };
    }
}
