package ru.hzerr.collections.functions;

public interface Functions {

    @FunctionalInterface
    interface Consumer<T, TH extends Exception> {

        void accept(T t) throws TH;
    }
    @FunctionalInterface
    interface BiConsumer<T, TH extends Exception, TH2 extends Exception> {

        void accept(T t) throws TH, TH2;
    }
    @FunctionalInterface
    interface ThConsumer<T, TH extends Exception, TH2 extends Exception, TH3 extends Exception> {

        void accept(T t) throws TH, TH2, TH3;
    }
    @FunctionalInterface
    interface Func<T, R, TH extends Exception> {

        R apply(T t) throws TH;
    }
    @FunctionalInterface
    interface BiFunc<T, R, TH extends Exception, TH2 extends Exception> {

        R apply(T t) throws TH, TH2;
    }
    @FunctionalInterface
    interface ThFunc<T, R, TH extends Exception, TH2 extends Exception, TH3 extends Exception> {

        R apply(T t) throws TH, TH2, TH3;
    }

    @FunctionalInterface
    interface Predicate<T, TH extends Exception> {

        boolean test(T t) throws TH;
    }
    @FunctionalInterface
    interface BiPredicate<T, TH extends Exception, TH2 extends Exception> {

        boolean test(T t) throws TH, TH2;
    }
    @FunctionalInterface
    interface ThPredicate<T, TH extends Exception, TH2 extends Exception, TH3 extends Exception> {

        boolean test(T t) throws TH, TH2, TH3;
    }
}
