package ru.hzerr.file;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.hzerr.file.exception.ValidationException;
import ru.hzerr.file.exception.file.HFileRenameFailedException;
import ru.hzerr.stream.BaseHStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collection;

public abstract class BaseFile implements IFSObject {

    protected File file;

    public BaseFile(String pathname) {
        this.file = new File(pathname);
        if (file.isDirectory()) throw new ValidationException(file + " is a directory");
    }

    public BaseFile(String parent, String child) {
        this.file = new File(parent, child);
        if (file.isDirectory()) throw new ValidationException(file + " is a directory");
    }

    public BaseFile(BaseDirectory parent, String child) {
        this.file = new File(parent.directory, child);
        if (file.isDirectory()) throw new ValidationException(file + " is a directory");
    }

    public BaseFile(URI uri) {
        this.file = new File(uri);
        if (file.isDirectory()) throw new ValidationException(file + " is a directory");
    }

    // METHODS

    public abstract void rename(String fullName) throws HFileRenameFailedException;
    public abstract void rename(String name, String extension) throws HFileRenameFailedException;
    public abstract <T extends BaseFile> void copyToFile(T file) throws IOException;
    public abstract <T extends BaseDirectory> void copyToDirectory(T directory) throws IOException;
    public abstract <T extends BaseFile> void moveToFile(T file) throws IOException;
    public abstract <T extends BaseDirectory> void moveToDirectory(T directory) throws IOException;
    public abstract byte[] readToByteArray() throws IOException;
    public abstract BaseHStream<String, ?> readLines(Charset charset) throws IOException;
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

        if (!(o instanceof BaseFile that)) return false;

        return file.equals(that.file);
    }

    @Override
    public boolean notEquals(Object o) { return !equals(o); }

    @Override
    public int hashCode() { return file.hashCode(); }
}
