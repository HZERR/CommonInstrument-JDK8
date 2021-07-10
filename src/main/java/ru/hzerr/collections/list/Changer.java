package ru.hzerr.collections.list;

import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public interface Changer<T> {

    void changeIf(UnaryOperator<T> changer, Predicate<T> condition);
}
