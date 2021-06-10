package ru.hzerr.file.stream;

import ru.hzerr.file.IFSObject;
import ru.hzerr.stream.function.BinaryOperator;
import ru.hzerr.stream.function.Consumer;
import ru.hzerr.stream.function.Predicate;

import java.util.Comparator;
import java.util.Optional;

public interface BaseFileStream<ID, IF, S extends BaseFileStream<ID, IF, S>> extends IFileWrapper {

    S fileFilter(Predicate<? super IF> condition);
    S dirFilter(Predicate<? super ID> condition);
    S filter(Predicate<? super IFSObject> condition);

    S fileForEach(Consumer<? super IF> action);
    S dirForEach(Consumer<? super ID> action);
    S forEach(Consumer<? super IFSObject> action);

    S fileForEachOrdered(Consumer<? super IF> action);
    S dirForEachOrdered(Consumer<? super ID> action);
    S forEachOrdered(Consumer<? super IFSObject> action);

    S filePeek(Consumer<? super IF> action);
    S dirPeek(Consumer<? super ID> action);
    S peek(Consumer<? super IFSObject> action);

    boolean fileAllMatch(Predicate<? super IF> action);
    boolean dirAllMatch(Predicate<? super ID> action);
    boolean allMatch(Predicate<? super IFSObject> action);

    boolean fileAnyMatch(Predicate<? super IF> action);
    boolean dirAnyMatch(Predicate<? super ID> action);
    boolean anyMatch(Predicate<? super IFSObject> action);

    boolean fileNoneMatch(Predicate<? super IF> action);
    boolean dirNoneMatch(Predicate<? super ID> action);
    boolean noneMatch(Predicate<? super IFSObject> action);

    S parallel(IFSObjects whichOneToApply);
    S parallelIfNeeded();
    boolean isParallel(IFSObjects whichOneToWatch);

    Optional<IF> fileFindFirst();
    Optional<ID> dirFindFirst();
    Optional<IFSObject> findFirst();
    Optional<IF> fileFindAny();
    Optional<ID> dirFindAny();
    Optional<IFSObject> findAny();

    Optional<IF> fileReduce(BinaryOperator<IF> accumulator);
    Optional<ID> dirReduce(BinaryOperator<ID> accumulator);

    Optional<IF> fileMin(Comparator<? super IF> comparator);
    Optional<ID> dirMin(Comparator<? super ID> comparator);
    Optional<IFSObject> min(Comparator<? super IFSObject> comparator);
    Optional<IF> fileMax(Comparator<? super IF> comparator);
    Optional<ID> dirMax(Comparator<? super ID> comparator);
    Optional<IFSObject> max(Comparator<? super IFSObject> comparator);

    S fileDistinct();
    S dirDistinct();
    S distinct();

    S fileSorted();
    S dirSorted();
    S sorted();
    S fileSorted(Comparator<? super IF> comparator);
    S dirSorted(Comparator<? super ID> comparator);
    S sorted(Comparator<? super IFSObject> comparator);

    S fileUnordered();
    S dirUnordered();
    S unordered();

    S fileSkip(long n);
    S dirSkip(long n);
    S skip(long n);
    S fileLimit(long maxSize);
    S dirLimit(long maxSize);
    S limit(long maxSize);

    long fileCount();
    long dirCount();
    long count();


}
