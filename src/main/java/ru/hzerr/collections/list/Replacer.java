package ru.hzerr.collections.list;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public interface Replacer<T> {

    void replaceIf(Predicate<? super T> condition, UnaryOperator<T> replacer);
}
