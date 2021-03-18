package ru.hzerr.stream.function;

public interface BiForEach<T> {

    /**
     * Performs the action specified by the condition for each group of elements
     * @param condition condition that divides the elements into two groups
     * @param actionForElementsYes action, for elements satisfying the condition
     * @param actionForElementsNo action, for elements that do not satisfy the condition
     */
    void biForEach(Predicate<? super T> condition,
                   Consumer<? super T> actionForElementsYes,
                   Consumer<? super T> actionForElementsNo);
}
