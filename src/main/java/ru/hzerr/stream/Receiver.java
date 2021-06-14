package ru.hzerr.stream;

import org.jetbrains.annotations.NotNull;
import ru.hzerr.stream.function.Supplier;

public interface Receiver<T> extends Supplier<T> {

    T onError();

    static <T> java.util.function.Supplier<T> convert(@NotNull Receiver<T> receiver) {
        return () -> {
            try {
                return receiver.get();
            } catch (Exception e) { return receiver.onError(); }
        };
    }

    interface Void<T> extends Supplier<T> {

        void onError();

        static <T> java.util.function.Supplier<T> convert(@NotNull Receiver.Void<T> receiver) {
            return () -> {
                try {
                    return receiver.get();
                } catch (Exception e) { receiver.onError(); throw new RuntimeException(e); }
            };
        }
    }
}
