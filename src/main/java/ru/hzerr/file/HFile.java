package ru.hzerr.file;

import org.apache.commons.io.FileUtils;
import ru.hzerr.file.exception.ValidationException;
import ru.hzerr.file.exception.directory.HDirectoryNotFoundException;
import ru.hzerr.file.exception.file.*;
import ru.hzerr.stream.HStream;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Objects;

public class HFile {

    File file;

    public HFile(String pathname) {
        this.file = new File(pathname);
        validate();
    }

    public HFile(String parent, String child) {
        this.file = new File(parent, child);
        validate();
    }

    public HFile(HDirectory parent, String child) {
        this.file = new File(parent.directory, child);
        validate();
    }

    public HFile(URI uri) {
        this.file = new File(uri);
        validate();
    }

    public String getFileName() { return this.file.getName(); }

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
        validate();
    }

    public void delete() throws IOException {
        checkExists(this);
        FileUtils.forceDelete(file);
    }

    public void copyToFile(HFile file) throws IOException {
        checkExists(file, this);
        FileUtils.copyFile(this.file, file.file);
    }

    public void copyToDirectory(HDirectory directory) throws IOException {
        checkExists(directory, this);
        FileUtils.copyToDirectory(file, directory.directory);
    }

    public void moveToFile(HFile file) throws IOException {
        checkExists(file, this);
        FileUtils.moveFile(this.file, file.file);
    }

    public void moveToDirectory(HDirectory directory) throws IOException {
        checkExists(directory, this);
        FileUtils.moveFileToDirectory(file, directory.directory, false);
    }

    public HDirectory getParent() { return new HDirectory(file.getAbsoluteFile().getParent()); }

    public boolean exists() { return file.exists(); }
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

    @Override
    public String toString() { return file.getAbsolutePath(); }

    private void validate() throws ValidationException {
        if (file.isDirectory())
            throw new ValidationException(file + " is a directory");
    }

    private void checkExists(HFile... files) throws HFileNotFoundException {
        Objects.requireNonNull(files, "Files");
        for (HFile file : files) {
            if (file.notExists())
                throw new HFileNotFoundException("File does not exist: " + file);
        }
    }

    private void checkExists(HDirectory... directories) throws HDirectoryNotFoundException {
        Objects.requireNonNull(directories, "Directories");
        for (HDirectory directory : directories) {
            if (directory.notExists())
                throw new HDirectoryNotFoundException("Directory does not exist: " + directory);
        }
    }

    private void checkExists(HDirectory directory, HFile file) throws HFileNotFoundException, HDirectoryNotFoundException {
        checkExists(directory);
        checkExists(file);
    }
}
