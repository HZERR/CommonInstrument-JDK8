package ru.hzerr.file.stream;

import ru.hzerr.file.HDirectory;
import ru.hzerr.file.HFile;
import ru.hzerr.file.IFSObject;
import ru.hzerr.stream.HStream;
import ru.hzerr.stream.function.BinaryOperator;
import ru.hzerr.stream.function.Consumer;
import ru.hzerr.stream.function.Predicate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class FileStream implements BaseFileStream<HDirectory, HFile, FileStream> {

    protected HStream<HDirectory> directories;
    protected HStream<HFile> files;

    FileStream(HStream<HDirectory> directories, HStream<HFile> files) {
        this.directories = directories;
        this.files = files;
    }

    @Override
    public FileStream fileFilter(Predicate<? super HFile> condition) {
        files.filter(condition);
        return this;
    }

    @Override
    public FileStream dirFilter(Predicate<? super HDirectory> condition) {
        directories.filter(condition);
        return this;
    }

    @Override
    public FileStream filter(Predicate<? super IFSObject> condition) {
        files.filter(condition);
        directories.filter(condition);
        return this;
    }

    @Override
    public FileStream fileForEach(Consumer<? super HFile> action) {
        files.forEach(action);
        return this;
    }

    @Override
    public FileStream dirForEach(Consumer<? super HDirectory> action) {
        directories.forEach(action);
        return this;
    }

    @Override
    public FileStream forEach(Consumer<? super IFSObject> action) {
        files.forEach(action);
        directories.forEach(action);
        return this;
    }

    @Override
    public FileStream fileForEachOrdered(Consumer<? super HFile> action) {
        files.forEachOrdered(action);
        return this;
    }

    @Override
    public FileStream dirForEachOrdered(Consumer<? super HDirectory> action) {
        directories.forEachOrdered(action);
        return this;
    }

    @Override
    public FileStream forEachOrdered(Consumer<? super IFSObject> action) {
        files.forEachOrdered(action);
        directories.forEachOrdered(action);
        return this;
    }

    @Override
    public FileStream filePeek(Consumer<? super HFile> action) {
        files.peek(action);
        return this;
    }

    @Override
    public FileStream dirPeek(Consumer<? super HDirectory> action) {
        directories.peek(action);
        return this;
    }

    @Override
    public FileStream peek(Consumer<? super IFSObject> action) {
        files.peek(action);
        directories.peek(action);
        return this;
    }

    @Override
    public boolean fileAllMatch(Predicate<? super HFile> action) {
        return files.allMatch(action);
    }

    @Override
    public boolean dirAllMatch(Predicate<? super HDirectory> action) {
        return directories.allMatch(action);
    }

    @Override
    public boolean allMatch(Predicate<? super IFSObject> action) {
        return files.allMatch(action) && directories.allMatch(action);
    }

    @Override
    public boolean fileAnyMatch(Predicate<? super HFile> action) {
        return files.anyMatch(action);
    }

    @Override
    public boolean dirAnyMatch(Predicate<? super HDirectory> action) {
        return directories.anyMatch(action);
    }

    @Override
    public boolean anyMatch(Predicate<? super IFSObject> action) {
        return files.anyMatch(action) || directories.anyMatch(action);
    }

    @Override
    public boolean fileNoneMatch(Predicate<? super HFile> action) {
        return files.noneMatch(action);
    }

    @Override
    public boolean dirNoneMatch(Predicate<? super HDirectory> action) {
        return directories.noneMatch(action);
    }

    @Override
    public boolean noneMatch(Predicate<? super IFSObject> action) {
        return files.noneMatch(action) && directories.noneMatch(action);
    }

    @Override
    public FileStream parallel(IFSObjects whichOneToApply) {
        switch (whichOneToApply) {
            case FILE -> files.parallel();
            case DIRECTORY -> directories.parallel();
            case ALL -> {
                files.parallel();
                directories.parallel();
            }
        };

        return this;
    }

    @Override
    public FileStream parallelIfNeeded() {
        files.parallelIfNeeded();
        directories.parallelIfNeeded();
        return this;
    }

    @Override
    public boolean isParallel(IFSObjects whichOneToWatch) {
        return switch (whichOneToWatch) {
            case FILE -> files.isParallel();
            case DIRECTORY -> directories.isParallel();
            case ALL -> files.isParallel() && directories.isParallel();
        };
    }

    @Override
    public Optional<HFile> fileFindFirst() { return files.findFirst(); }

    @Override
    public Optional<HDirectory> dirFindFirst() { return directories.findFirst(); }

    @Override
    public Optional<IFSObject> findFirst() {
        final Optional<HFile> findFirstFile = files.findFirst();
        if (findFirstFile.isPresent()) {
            return Optional.of(findFirstFile.get());
        }
        final Optional<HDirectory> findFirstDirectory = directories.findFirst();
        //noinspection OptionalIsPresent
        return findFirstDirectory.isPresent() ? Optional.of(findFirstDirectory.get()) : Optional.empty();
    }

    @Override
    public Optional<HFile> fileFindAny() { return files.findAny(); }

    @Override
    public Optional<HDirectory> dirFindAny() { return directories.findAny(); }

    @Override
    public Optional<IFSObject> findAny() {
        final Optional<HFile> findAnyFile = files.findAny();
        if (findAnyFile.isPresent()) {
            return Optional.of(findAnyFile.get());
        }
        final Optional<HDirectory> findFirstDirectory = directories.findAny();
        //noinspection OptionalIsPresent
        return findFirstDirectory.isPresent() ? Optional.of(findFirstDirectory.get()) : Optional.empty();
    }

    @Override
    public Optional<HFile> fileReduce(BinaryOperator<HFile> accumulator) { return files.reduce(accumulator); }

    @Override
    public Optional<HDirectory> dirReduce(BinaryOperator<HDirectory> accumulator) { return directories.reduce(accumulator); }

    @Override
    public Optional<HFile> fileMin(Comparator<? super HFile> comparator) { return files.min(comparator); }

    @Override
    public Optional<HDirectory> dirMin(Comparator<? super HDirectory> comparator) { return directories.min(comparator); }

    @SuppressWarnings("OptionalIsPresent")
    @Override
    public Optional<IFSObject> min(Comparator<? super IFSObject> comparator) {
        final Optional<HFile> fileMin = files.min(comparator);
        final Optional<HDirectory> dirMin = directories.min(comparator);
        if (fileMin.isPresent() && dirMin.isPresent()) {
            int comparisonResult = comparator.compare(fileMin.get(), dirMin.get());
            return comparisonResult < 1 ? Optional.of(fileMin.get()) : Optional.of(dirMin.get());
        }

        return fileMin.isPresent() ? Optional.of(fileMin.get())
                : dirMin.isPresent() ? Optional.of(dirMin.get())
                : Optional.empty();
    }

    @Override
    public Optional<HFile> fileMax(Comparator<? super HFile> comparator) { return files.max(comparator); }

    @Override
    public Optional<HDirectory> dirMax(Comparator<? super HDirectory> comparator) { return directories.max(comparator); }

    @Override
    public Optional<IFSObject> max(Comparator<? super IFSObject> comparator) {
        final Optional<HFile> fileMax = files.max(comparator);
        final Optional<HDirectory> dirMax = directories.max(comparator);
        if (fileMax.isPresent() && dirMax.isPresent()) {
            int comparisonResult = comparator.compare(fileMax.get(), dirMax.get());
            return comparisonResult > 0 ? Optional.of(fileMax.get()) : Optional.of(dirMax.get());
        }

        return fileMax.isPresent() ? Optional.of(fileMax.get())
                : dirMax.isPresent() ? Optional.of(dirMax.get())
                : Optional.empty();
    }

    @Override
    public FileStream fileDistinct() {
        files.distinct();
        return this;
    }

    @Override
    public FileStream dirDistinct() {
        directories.distinct();
        return this;
    }

    @Override
    public FileStream distinct() {
        files.distinct();
        directories.distinct();
        return this;
    }

    @Override
    public FileStream fileSorted() {
        files.sorted();
        return this;
    }

    @Override
    public FileStream dirSorted() {
        directories.sorted();
        return this;
    }

    @Override
    public FileStream sorted() {
        files.sorted();
        directories.sorted();
        return this;
    }

    @Override
    public FileStream fileSorted(Comparator<? super HFile> comparator) {
        files.sorted(comparator);
        return this;
    }

    @Override
    public FileStream dirSorted(Comparator<? super HDirectory> comparator) {
        directories.sorted(comparator);
        return this;
    }

    @Override
    public FileStream sorted(Comparator<? super IFSObject> comparator) {
        files.sorted(comparator);
        directories.sorted(comparator);
        return this;
    }

    @Override
    public FileStream fileUnordered() {
        files.unordered();
        return this;
    }

    @Override
    public FileStream dirUnordered() {
        directories.unordered();
        return this;
    }

    @Override
    public FileStream unordered() {
        files.unordered();
        directories.unordered();
        return this;
    }

    @Override
    public FileStream fileSkip(long n) {
        files.skip(n);
        return this;
    }

    @Override
    public FileStream dirSkip(long n) {
        directories.skip(n);
        return this;
    }

    @Override
    public FileStream skip(long n) {
        files.skip(n);
        directories.skip(n);
        return this;
    }

    @Override
    public FileStream fileLimit(long maxSize) {
        files.limit(maxSize);
        return this;
    }

    @Override
    public FileStream dirLimit(long maxSize) {
        directories.limit(maxSize);
        return this;
    }

    @Override
    public FileStream limit(long maxSize) {
        files.limit(maxSize);
        directories.limit(maxSize);
        return this;
    }

    @Override
    public long fileCount() { return files.count(); }

    @Override
    public long dirCount() { return directories.count(); }

    @Override
    public long count() { return files.count() + directories.count(); }

    @Override
    public void wrap(IFSObjects whichOneToApply, java.util.function.Consumer<Exception> onError) {
        switch (whichOneToApply) {
            case FILE -> files.wrap(onError);
            case DIRECTORY -> directories.wrap(onError);
            case ALL -> {
                files.wrap(onError);
                directories.wrap(onError);
            }
        }
    }

    public static FileStream of(HStream<Path> objects) {
        final List<HDirectory> dirs = new ArrayList<>();
        final List<HFile> files = new ArrayList<>();
        objects.biForEach(
                Files::isDirectory,
                o -> dirs.add(new HDirectory(o.toString())),
                o -> files.add(new HFile(o.toString()))
        );

        return new FileStream(HStream.of(dirs), HStream.of(files));
    }

    public static FileStream of(Stream<Path> objects) {
        final List<HDirectory> dirs = new ArrayList<>();
        final List<HFile> files = new ArrayList<>();
        objects.forEach(path -> {
            if (Files.isDirectory(path)) {
                dirs.add(new HDirectory(path.toString()));
            } else
                files.add(new HFile(path.toString()));
        });

        return new FileStream(HStream.of(dirs), HStream.of(files));
    }

    public static FileStream of(IFSObject... objects) { return of(HStream.of(objects).map(IFSObject::asPath)); }
    public static FileStream of(Iterator<Path> iterator) { return of(HStream.of(iterator)); }
    public static FileStream of(Iterable<Path> iterable) { return of(HStream.of(iterable)); }
    public static FileStream of(Spliterator<Path> spliterator) { return of(HStream.of(spliterator)); }
}
