package ru.hzerr.file;

import org.apache.commons.io.FileUtils;
import ru.hzerr.file.exception.directory.NoSuchHDirectoryException;
import ru.hzerr.file.exception.file.*;
import ru.hzerr.stream.HStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class HFile extends BaseFile {

    public HFile(String pathname) { super(pathname); }
    public HFile(String parent, String child) { super(parent, child); }
    public HFile(BaseDirectory parent, String child) { super(parent, child); }
    public HFile(URI uri) { super(uri); }

    @Override
    public String getName() { return this.file.getName(); }

    @Override
    public void create() throws HFileIsNotFileException, HFileCreationFailedException, HFileCreateImpossibleException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new HFileIsNotFileException("File " + file + " exists and is not a file. Unable to create file.");
            }
        } else {
            boolean created;
            try {
                created = file.createNewFile();
            } catch (IOException io) { throw new HFileCreationFailedException(io.getMessage()); }
            if (!created) {
                // Double-check that some other thread or process hasn't made
                // the file in the background
                if (file.isDirectory()) {
                    throw new HFileCreateImpossibleException("Unable to create file " + file);
                }
            }
        }
    }

    @Override
    public boolean delete() throws IOException {
        checkExists(this);
        FileUtils.forceDelete(file);
        return notExists();
    }

    @Override
    public <T extends BaseFile>
    void copyToFile(T file) throws IOException {
        checkExists(file, this);
        FileUtils.copyFile(this.file, file.file);
    }

    @Override
    public <T extends BaseDirectory>
    void copyToDirectory(T directory) throws IOException {
        checkExists(directory, this);
        FileUtils.copyToDirectory(file, directory.directory);
    }

    @Override
    public <T extends BaseFile>
    void moveToFile(T file) throws IOException {
        checkExists(file, this);
        FileUtils.moveFile(this.file, ((HFile) file).file);
    }

    @Override
    public <T extends BaseDirectory>
    void moveToDirectory(T directory) throws IOException {
        checkExists(directory, this);
        FileUtils.moveFileToDirectory(file, ((HDirectory) directory).directory, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public HDirectory getParent() { return new HDirectory(file.getAbsoluteFile().getParent()); }

    @Override
    public <T extends BaseDirectory> boolean isHierarchicalChild(T superParent) {
        try {
            return isHierarchicalChild0(superParent);
        } catch (NullPointerException npe) { return false; }
    }

    @Override
    public <T extends BaseDirectory> boolean notIsHierarchicalChild(T superParent) {
        return !isHierarchicalChild(superParent);
    }

    @Override
    public boolean exists() { return file.exists(); }
    @Override
    public boolean notExists() { return !file.exists(); }

    public byte[] readToByteArray() throws HFileReadException {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException io) {
            throw new HFileReadException(io, "The reading of file " + toString() + " ended with an error");
        }
    }

    public HStream<String> readLines(Charset charset) throws HFileReadException {
        try {
            return HStream.of(FileUtils.readLines(file, charset));
        } catch (IOException io) {
            throw new HFileReadException(io, "The reading of file " + toString() + " ended with an error");
        }
    }

    @Override
    void writeLines(String... lines) throws IOException { writeLines(List.of(lines)); }

    public void writeLines(Collection<String> lines) throws HFileWriteException {
        writeLines(lines, false);
    }

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
    public void writeLines(Collection<String> lines, boolean append) throws HFileWriteException {
        try {
            FileUtils.writeLines(file, lines, append);
        } catch (IOException io) {
            throw new HFileWriteException(io, "Writing to file " + toString() + " ended with an error");
        }
    }

    public long checksum() throws HFileReadException {
        try {
            return FileUtils.checksumCRC32(this.file);
        } catch (IOException io) { throw new HFileReadException(io, "The checksum can't be received"); }
    }

    public InputStream openInputStream() throws HFileNotFoundException, HFileReadException {
        try {
            return FileUtils.openInputStream(file);
        } catch (FileNotFoundException fnf) {
            throw new HFileNotFoundException("File does not exist: " + file);
        } catch (IOException io) {
            throw new HFileReadException(io, "The reading of file " + toString() + " ended with an error");
        }
    }

    public OutputStream openOutputStream() throws IOException {
        return openOutputStream(false);
    }

    public OutputStream openOutputStream(boolean append) throws IOException {
        return FileUtils.openOutputStream(file, append);
    }

    public double sizeOf(SizeType type) {
        BigDecimal size = new BigDecimal(FileUtils.sizeOfAsBigInteger(file));
        return SizeType.BYTE.to(type, size).setScale(1, RoundingMode.DOWN).doubleValue();
    }

    private boolean isHierarchicalChild0(BaseDirectory superParent) {
        BaseDirectory superDirectory = getParent();
        while (superDirectory != null) {
            if (superDirectory.directory.equals(superParent.directory)) {
                return true;
            } else superDirectory = superDirectory.getParent();
        }

        return false;
    }

    private void checkExists(IFSObject... objects) {
        Objects.requireNonNull(objects, "Objects");
        for (IFSObject object : objects) {
            if (object.notExists()) {
                if (object instanceof BaseFile that) {
                    throw new NoSuchHFileException("File does not exist: " + that.file);
                } else if (object instanceof BaseDirectory that) {
                    throw new NoSuchHDirectoryException("Directory does not exist: " + that.directory);
                } else throw new IllegalArgumentException("IFSObjects don't inherit BaseFile or BaseDirectory!");
            }
        }
    }
}
