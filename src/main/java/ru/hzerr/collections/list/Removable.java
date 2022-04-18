package ru.hzerr.collections.list;

import ru.hzerr.collections.functions.Functions;

@SuppressWarnings("UnusedReturnValue")
public interface Removable<T> {

    <TH extends Exception>
    boolean removeIf(Functions.Predicate<? super T, TH> predicate, Class<TH> exception) throws TH;
    <TH extends Exception, TH2 extends Exception>
    boolean removeIf(Functions.BiPredicate<? super T, TH, TH2> predicate, Class<TH> exception, Class<TH2> exception2) throws TH, TH2;
    <TH extends Exception, TH2 extends Exception, TH3 extends Exception>
    boolean removeIf(Functions.ThPredicate<? super T, TH, TH2, TH3> predicate, Class<TH> exception, Class<TH2> exception2, Class<TH3> exception3) throws TH, TH2, TH3;
}
