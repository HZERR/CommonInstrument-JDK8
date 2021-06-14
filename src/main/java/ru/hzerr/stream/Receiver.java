package ru.hzerr.stream;

import org.jetbrains.annotations.NotNull;
import ru.hzerr.stream.function.Supplier;

public interface Receiver<T> extends Supplier<T> {

    T onError();

    default java.util.function.Supplier<T> asSupplier() {
        return () -> {
            try {
                return get();
            } catch (Exception e) { return onError(); }
        };
    }

    static <T> java.util.function.Supplier<T> convert(@NotNull Receiver<T> receiver) {
        return () -> {
            try {
                return receiver.get();
            } catch (Exception e) { return receiver.onError(); }
        };
    }

    interface Void<T> extends Supplier<T> {

        void onError();

        default java.util.function.Supplier<T> asSupplier() {
            return () -> {
                try {
                    return get();
                } catch (Exception e) { onError(); throw new RuntimeException(e); }
            };
        }

        static <T> java.util.function.Supplier<T> convert(@NotNull Receiver.Void<T> receiver) {
            return () -> {
                try {
                    return receiver.get();
                } catch (Exception e) { receiver.onError(); throw new RuntimeException(e); }
            };
        }
    }
}
