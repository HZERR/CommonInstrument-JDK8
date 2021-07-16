package ru.hzerr.collections.list;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Changer<T> {

    void changeIf(Predicate<? super T> condition, Consumer<? super T> changer);
}
