package ru.hzerr.stream.function;

import ru.hzerr.stream.BaseHStream;

public interface BiMap<T> {

    /**
     * Converts the items specified by the condition for each group and returns a new HStream
     * @param condition condition that divides the elements into two groups
     * @param actionForElementsYes action, for elements satisfying the condition
     * @param actionForElementsNo action, for elements that do not satisfy the condition
     * @return new HStream<R>
     */
    <R> BaseHStream biMap(Predicate<? super T> condition,
                             Function<? super T, ? extends R> actionForElementsYes,
                             Function<? super T, ? extends R> actionForElementsNo);
}
