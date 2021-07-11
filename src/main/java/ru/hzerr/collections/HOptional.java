package ru.hzerr.collections;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author HZERR
 * @param <T> the type of value
 */
@SuppressWarnings({"unused", "ClassCanBeRecord"})
public class HOptional<T> {

    private static final HOptional<?> EMPTY = new HOptional<>(null);
    private final T value;

    public HOptional(T value) {
        this.value = value;
    }

    public static <T> HOptional<T> empty() {
        @SuppressWarnings("unchecked")
        HOptional<T> t = (HOptional<T>) EMPTY;
        return t;
    }

    public static <T> HOptional<T> of(T value) {
        return new HOptional<>(Objects.requireNonNull(value));
    }

    @SuppressWarnings("unchecked")
    public static <T> HOptional<T> ofNullable(T value) {
        return value == null ? (HOptional<T>) EMPTY
                : new HOptional<>(value);
    }

    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }

        return value;
    }

    public void ifPresent(Consumer<? super T> action) {
        if (value != null) {
            action.accept(value);
        }
    }

    public void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
        if (value != null) {
            action.accept(value);
        } else {
            emptyAction.run();
        }
    }

    public HOptional<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (!isPresent()) {
            return this;
        } else {
            return predicate.test(value) ? this : empty();
        }
    }

    public <U> HOptional<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent()) {
            return empty();
        } else {
            return HOptional.ofNullable(mapper.apply(value));
        }
    }

    public <U> HOptional<U> flatMap(Function<? super T, ? extends HOptional<? extends U>> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent()) {
            return empty();
        } else {
            @SuppressWarnings("unchecked")
            HOptional<U> r = (HOptional<U>) mapper.apply(value);
            return Objects.requireNonNull(r);
        }
    }

    public HOptional<T> or(Supplier<? extends HOptional<? extends T>> supplier) {
        Objects.requireNonNull(supplier);
        if (isPresent()) {
            return this;
        } else {
            @SuppressWarnings("unchecked")
            HOptional<T> r = (HOptional<T>) supplier.get();
            return Objects.requireNonNull(r);
        }
    }

    public T orElse(T other) {
        return value != null ? value : other;
    }

    /**
     * Example:
     * <pre><code>
     * public class Response {
     * }
     *
     * public class TokenResponse extends Response {
     * }
     *
     * public class CaptchaResponse extends Response {
     * }
     *
     * public class ErrorResponse extends Response {
     * }
     *
     * public class Main {
     *     public static void main(String[] args) {
     *         System.out.println(getResponse());
     *     }
     *
     *     public static Response getResponse() {
     *         HOptional<TokenResponse> tokenResponse = HOptional.of(new TokenResponse());
     *         HOptional<CaptchaResponse> captchaResponse = HOptional.ofNullable(new CaptchaResponse());
     *         HOptional<ErrorResponse> errorResponse = HOptional.ofNullable(new ErrorResponse());
     *         return success.or(warn, info);
     *     }
     * }
     * </code></pre>
     *
     * @throws NullPointerException if all values is null
     * @throws ClassCastException   if class "A" can't be cast to class "B"
     */
    @SafeVarargs
    public final <S, R extends S>
    R or(HOptional<? extends R>... optionals) {
        if (isPresent()) //noinspection unchecked
            return (R) value;
        for (HOptional<? extends R> opt : optionals) {
            if (opt.isPresent()) {
                return opt.get();
            }
        }

        throw new NullPointerException();
    }

    @SafeVarargs
    public final <S, R extends S, X extends Throwable>
    R orElseThrow(Supplier<? extends X> exceptionSupplier, HOptional<? extends R>... optionals) throws X {
        if (isPresent()) //noinspection unchecked
            return (R) value;
        for (HOptional<? extends R> opt : optionals) {
            if (opt.isPresent()) {
                return opt.get();
            }
        }

        throw exceptionSupplier.get();
    }

    public T orElseGet(Supplier<? extends T> supplier) {
        return value != null ? value : supplier.get();
    }

    public T orElseThrow() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    public boolean isPresent() {
        return value != null;
    }

    public boolean isEmpty() {
        return value == null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof HOptional<?>)) return false;

        return Objects.equals(value, ((HOptional<?>) obj).value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value != null
                ? String.format("Optional[%s]", value)
                : "Optional.empty";
    }
}
