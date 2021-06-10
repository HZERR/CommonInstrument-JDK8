package ru.hzerr.stream.function;

import ru.hzerr.stream.BaseHStream;

public interface BiFilter<T> {

    /**
     * Filters the items specified by the condition for each group and returns the current HStream
     * @param condition condition that divides the elements into two groups
     * @param actionForElementsYes action, for elements satisfying the condition
     * @param actionForElementsNo action, for elements that do not satisfy the condition
     * @return current HStream<R>
     */
    BaseHStream biFilter(Predicate<? super T> condition,
                         Predicate<? super T> actionForElementsYes,
                         Predicate<? super T> actionForElementsNo);
}
