package ru.hzerr.bytecode;

import java.util.concurrent.Callable;

class Runtime {

    public static void run(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <V> V call(Callable<V> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void runIsNotNull(Object obj, Runnable runnable) {
        if (runnable == null) throw new NullPointerException("Runnable cannot be null");
        if (obj != null) {
            run(runnable);
        }
    }

    public static <V> V callIsNotNull(Object obj, Callable<V> callable) {
        if (callable == null) throw new NullPointerException("Callable cannot be null");
        if (obj != null) {
            return call(callable);
        }

        return null;
    }
}
