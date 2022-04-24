package ru.hzerr.file;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.hzerr.collections.list.HList;
import ru.hzerr.file.exception.ValidationException;
import ru.hzerr.file.exception.file.ByteBufferNotInitializationException;
import ru.hzerr.file.exception.file.HFileRenameFailedException;
import ru.hzerr.file.exception.file.HFileWriteException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public abstract class BaseFile implements IFSObject {

    protected File file;

    public BaseFile(String pathname) {
        this.file = new File(pathname);
        if (file.isDirectory()) throw new ValidationException(file + " is a directory");
    }

    public BaseFile(String parent, String child) {
        this.file = new File(Paths.get(parent).resolve(child).normalize().toString());
        if (file.isDirectory()) throw new ValidationException(file + " is a directory");
    }

    public BaseFile(BaseDirectory parent, String child) {
        this.file = new File(Paths.get(parent.getLocation()).resolve(child).normalize().toString());
        if (file.isDirectory()) throw new ValidationException(file + " is a directory");
    }

    public BaseFile(URI uri) {
        this.file = new File(uri);
        if (file.isDirectory()) throw new ValidationException(file + " is a directory");
    }

    // METHODS

    public abstract String getExtension();
    public abstract String getBaseName();
    public abstract <T extends BaseFile> boolean equalsExtension(T file);
    public abstract <T extends BaseFile> boolean equalsBaseName(T file);
    public abstract <T extends BaseFile> boolean notEqualsExtension(T file);
    public abstract <T extends BaseFile> boolean notEqualsBaseName(T file);

    /**
     * @see java.nio.file.Files#move(Path, Path, CopyOption...)
     */
    public abstract void rename(String fullName) throws HFileRenameFailedException;
    public abstract void rename(String name, String extension) throws HFileRenameFailedException;
    public abstract <T extends BaseFile> void copyToFile(T file) throws IOException;
    public abstract <T extends BaseDirectory> void copyToDirectory(T directory) throws IOException;
    public abstract <T extends BaseFile> void moveToFile(T file) throws IOException;
    public abstract <T extends BaseDirectory> void moveToDirectory(T directory) throws IOException;
    public abstract byte[] readToByteArray() throws IOException;
    public abstract HList<String> readLines(Charset charset) throws IOException;
    public abstract HList<String> asyncReadLines(Charset charset) throws IOException;
    public abstract void refreshDataInMemory() throws IOException;
    public abstract HList<String> readFromMemory(Charset charset);
    public abstract void cleanDataInMemory();

    /**
     * Writes the <code>toString()</code> value of each item in a collection to
     * the specified <code>File</code> line by line.
     * The default VM encoding and the default line ending will be used.
     *
     * @param lines  the lines to write, {@code null} entries produce blank lines
     * @param append if {@code true}, then the lines will be added to the
     *               end of the file rather than overwriting
     * @throws HFileWriteException in case of a writing error
     * @since 2.1
     */
    public abstract void writeLines(Collection<String> lines, boolean append) throws IOException;
    public abstract void writeLines(Collection<String> lines) throws IOException;
    public abstract void writeLines(String... lines) throws IOException;
    public abstract long checksum() throws IOException;
    public abstract InputStream openInputStream() throws IOException;
    public abstract OutputStream openOutputStream() throws IOException;
    public abstract OutputStream openOutputStream(boolean append) throws IOException;

    // END METHODS

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("file", file)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof BaseFile)) return false;

        return file.equals(((BaseFile) o).file);
    }

    @Override
    public boolean notEquals(Object o) { return !equals(o); }

    @Override
    public int hashCode() { return file.hashCode(); }
}
