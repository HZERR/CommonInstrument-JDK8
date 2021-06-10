package ru.hzerr.file;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.hzerr.file.exception.ValidationException;
import ru.hzerr.stream.HStream;
import ru.hzerr.file.stream.FileStream;

import java.io.File;
import java.io.IOException;
import java.net.URI;

@SuppressWarnings({"unused", "unchecked"})
public abstract class BaseDirectory implements IFSObject {

    protected File directory;

    public BaseDirectory(String pathname) {
        this.directory = new File(pathname);
        if (directory.isFile()) throw new ValidationException(directory + " is a file");
    }

    public BaseDirectory(String parent, String child) {
        this.directory = new File(parent, child);
        if (directory.isFile()) throw new ValidationException(directory + " is a file");
    }

    public BaseDirectory(URI uri) {
        this.directory = new File(uri);
        if (directory.isFile()) throw new ValidationException(directory + " is a file");
    }

    public BaseDirectory(BaseDirectory parent, String child) {
        this.directory = new File(parent.directory, child);
        if (directory.isFile()) throw new ValidationException(directory + " is a file");
    }

    // METHODS

    public abstract <T extends BaseDirectory> T createSubDirectory(String dirName) throws IOException;
    public abstract <T extends BaseFile> T createSubFile(String fileName) throws IOException;
    public abstract <T extends BaseDirectory> T getSubDirectory(String dirName);
    public abstract <T extends BaseFile> T getSubFile(String fileName);

    public abstract <T extends BaseFile> HStream<T> getFiles() throws IOException;
    public abstract <T extends BaseDirectory> HStream<T> getDirectories() throws IOException;
    public abstract FileStream getFiles(boolean recursive) throws IOException;
    public abstract <T extends BaseFile> HStream<T> getFilesExcept(T... filesToBeExcluded) throws IOException;
    public abstract <T extends BaseFile> HStream<T> getFilesExcept(String... filesToBeExcluded) throws IOException;
    public abstract <T extends BaseDirectory> HStream<T> getDirectoriesExcept(T... filesToBeExcluded) throws IOException;
    public abstract <T extends BaseDirectory> HStream<T> getDirectoriesExcept(String... filesToBeExcluded) throws IOException;
    public abstract FileStream getFilesExcept(FileStream filesToBeExcluded, boolean recursive) throws IOException;

    public abstract boolean isEmpty();
    public abstract boolean isNotEmpty();
    public abstract boolean hasOnlyFiles() throws IOException;
    public abstract boolean hasOnlyDirectories() throws IOException;
    public abstract boolean isNotFoundInternalDirectories() throws IOException;
    public abstract boolean isNotFoundInternalFiles() throws IOException;
    public abstract boolean contains(IFSObject object, boolean recursive) throws IOException;
    public abstract boolean clean() throws IOException;

    public abstract <T extends BaseDirectory> boolean deleteExcept(T... directories) throws IOException;
    public abstract <T extends BaseFile> boolean deleteExcept(T... files) throws IOException;

    public abstract boolean deleteExcept(FileStream excludedFiles) throws IOException;
    public abstract boolean delete(String dirOrFileName) throws IOException;

    public abstract <T extends BaseDirectory>
    void copyToDirectory(T directory) throws IOException;
    public abstract <T extends BaseDirectory>
    void copyContentToDirectory(T directory) throws IOException;
    public abstract <T extends BaseDirectory>
    void moveToDirectory(T directory) throws IOException;
    public abstract <T extends BaseDirectory>
    void moveContentToDirectory(T directory) throws IOException;

    // END METHODS

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("directory", directory)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof BaseDirectory that)) return false;

        return directory.equals(that.directory);
    }

    @Override
    public boolean notEquals(Object o) { return !equals(o); }

    @Override
    public int hashCode() { return directory.hashCode(); }
}
