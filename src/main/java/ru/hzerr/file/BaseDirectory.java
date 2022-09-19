package ru.hzerr.file;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.hzerr.collections.list.HList;
import ru.hzerr.file.annotation.MaybeRecursive;
import ru.hzerr.file.annotation.NotRecursive;
import ru.hzerr.file.annotation.Recursive;
import ru.hzerr.file.exception.ValidationException;
import ru.hzerr.file.exception.directory.HDirectoryRenameFailedException;
import ru.hzerr.file.exception.directory.HDirectoryRenameImpossibleException;
import ru.hzerr.util.JsonToStringStyle;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Predicate;

@SuppressWarnings({"unused", "unchecked"})
public abstract class BaseDirectory implements IFSObject {

    protected File directory;

    public BaseDirectory(String pathname) {
        this.directory = new File(pathname);
        if (directory.isFile()) throw new ValidationException(directory + " is a file");
    }

    public BaseDirectory(String parent, String child) {
        this.directory = new File(Paths.get(parent).resolve(child).normalize().toString()); // fixed by HZERR
        if (directory.isFile()) throw new ValidationException(directory + " is a file");
    }

    public BaseDirectory(URI uri) {
        this.directory = new File(uri);
        if (directory.isFile()) throw new ValidationException(directory + " is a file");
    }

    public BaseDirectory(BaseDirectory parent, String child) {
        this.directory = new File(Paths.get(parent.getLocation()).resolve(child).normalize().toString()); // fixed by HZERR
        if (directory.isFile()) throw new ValidationException(directory + " is a file");
    }

    // METHODS

    public abstract <T extends BaseDirectory> T createSubDirectory(String dirName) throws IOException;
    public abstract <T extends BaseFile> T createSubFile(String fileName) throws IOException;
    public abstract <T extends BaseDirectory> T getSubDirectory(String dirName);
    public abstract <T extends BaseFile> T getSubFile(String fileName);

    public abstract void rename(String fullName) throws HDirectoryRenameFailedException, HDirectoryRenameImpossibleException;

    @NotRecursive public abstract HList<BaseFile> getFiles() throws IOException;
    @NotRecursive public abstract HList<BaseDirectory> getDirectories() throws IOException;
    @MaybeRecursive public abstract HList<IFSObject> getAllFiles(boolean recursive) throws IOException;
    @MaybeRecursive public abstract HList<BaseFile> getFiles(boolean recursive) throws IOException;
    @MaybeRecursive public abstract HList<BaseDirectory> getDirectories(boolean recursive) throws IOException;
    @NotRecursive public abstract <T extends BaseFile> HList<BaseFile> getFilesExcept(T... filesToBeExcluded) throws IOException;

    /**
     * Example of incoming arguments: java.exe, build.gradle.
     * Do not use names without a file extension
     */
    @NotRecursive public abstract <T extends BaseFile> HList<BaseFile> getFilesExcept(String... fileNamesToBeExcluded) throws IOException;
    @MaybeRecursive public abstract <T extends BaseFile> HList<BaseFile> getFilesExcept(boolean recursive, T... filesToBeExcluded) throws IOException;
    @NotRecursive public abstract <T extends BaseDirectory> HList<BaseDirectory> getDirectoriesExcept(T... filesToBeExcluded) throws IOException;
    @NotRecursive public abstract <T extends BaseDirectory> HList<BaseDirectory> getDirectoriesExcept(String... filesToBeExcluded) throws IOException;
    @MaybeRecursive public abstract <T extends BaseDirectory> HList<BaseDirectory> getDirectoriesExcept(boolean recursive, T... directoriesToBeExcluded) throws IOException;

    /**
     * Returns a list of all files in the current directory, except those that should be excluded
     * @param filesToBeExcluded files that should be excluded
     * @param recursive to use all internal files for the brute force or not
     * @return {@link HList<IFSObject>} files contained in the current directory, except those that have been excluded
     * @throws IOException in case of an I/O error
     */
    @MaybeRecursive public abstract HList<IFSObject> getAllFilesExcept(HList<IFSObject> filesToBeExcluded, boolean recursive) throws IOException;

    public abstract boolean isEmpty() throws IOException;
    public abstract boolean isNotEmpty() throws IOException;
    @NotRecursive public abstract boolean hasOnlyFiles() throws IOException;
    @NotRecursive public abstract boolean hasOnlyDirectories() throws IOException;
    @NotRecursive public abstract boolean notFoundInternalDirectories() throws IOException;
    @NotRecursive public abstract boolean notFoundInternalFiles() throws IOException;
    @MaybeRecursive public abstract boolean contains(IFSObject object, boolean recursive) throws IOException;
    @Recursive public abstract HList<IFSObject> find(Predicate<? super IFSObject> matcher) throws IOException;
    @Recursive public abstract HList<IFSObject> find(String glob) throws IOException;

    /**
     * Example of incoming method arguments: "java", "config.ini", "persons".
     */
    @Recursive public abstract HList<IFSObject> findByNames(String... names) throws IOException;
    @Recursive public abstract HList<BaseDirectory> findDirectories(Predicate<? super BaseDirectory> matcher) throws IOException;
    @Recursive public abstract HList<BaseDirectory> findDirectories(String glob) throws IOException;
    @Recursive public abstract HList<BaseDirectory> findDirectoriesByNames(String... names) throws IOException;
    @Recursive public abstract HList<BaseFile> findFiles(Predicate<? super BaseFile> matcher) throws IOException;
    @Recursive public abstract HList<BaseFile> findFiles(String glob) throws IOException;

    /**
     * Example of incoming method arguments: "java.exe", "config.ini".
     */
    @Recursive public abstract HList<BaseFile> findFilesByNames(String... names) throws IOException;

    /**
     * Clears the entire directory without deleting the current directory
     */
    public abstract void clean() throws IOException;

    @Recursive public abstract <T extends BaseDirectory> boolean deleteExcept(T... directories) throws IOException;
    @Recursive public abstract <T extends BaseFile> boolean deleteExcept(T... files) throws IOException;
    @Recursive public abstract boolean deleteExcept(HList<IFSObject> excludedFiles) throws IOException;
    @NotRecursive public abstract boolean delete(String dirOrFileName) throws IOException;

    public abstract <T extends BaseDirectory>
    void copyToDirectory(T directory) throws IOException;
    public abstract <T extends BaseDirectory>
    void copyContentToDirectory(T directory) throws IOException;
    public abstract <T extends BaseDirectory>
    void moveToDirectory(T directory) throws IOException;
    public abstract <T extends BaseDirectory>
    Optional<T> moveContentToDirectory(T directory) throws IOException;

    public abstract <T extends BaseDirectory>
    Optional<T> moveContentToDirectory(T directory, boolean delete) throws IOException;

    @NotRecursive public abstract boolean checkCountFiles(Long count) throws IOException;
    @NotRecursive public abstract boolean checkCountOnlyFiles(Long count) throws IOException;
    @NotRecursive public abstract boolean checkCountOnlyDirectories(Long count) throws IOException;
    @NotRecursive public abstract boolean hasOnly1File() throws IOException;
    @NotRecursive public abstract boolean hasOnly1File(String fileName) throws IOException;
    @NotRecursive public abstract boolean hasOnly1Directory() throws IOException;
    @NotRecursive public abstract boolean hasOnly1Directory(String directoryName) throws IOException;
    @NotRecursive public abstract boolean hasOnly1FileOrDirectory() throws IOException;

    // END METHODS

    @Override
    public String toString() {
        return new ToStringBuilder(this, new JsonToStringStyle())
                .append("directory", directory)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof BaseDirectory)) return false;

        return directory.equals(((BaseDirectory) o).directory);
    }

    @Override
    public boolean notEquals(Object o) { return !equals(o); }

    @Override
    public int hashCode() { return directory.hashCode(); }
}
