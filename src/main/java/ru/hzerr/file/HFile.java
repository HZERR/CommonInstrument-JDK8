package ru.hzerr.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import ru.hzerr.file.exception.ParentNotFoundException;
import ru.hzerr.file.exception.directory.NoSuchHDirectoryException;
import ru.hzerr.file.exception.file.*;
import ru.hzerr.stream.HStream;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class HFile extends BaseFile {

    public HFile(String pathname) { super(pathname); }
    public HFile(String parent, String child) { super(parent, child); }
    public HFile(BaseDirectory parent, String child) { super(parent, child); }
    public HFile(URI uri) { super(uri); }
    HFile(Path path) { super(path.toString()); }

    @Override
    public String getName() { return this.file.getName(); }

    @Override
    public String getExtension() { return FilenameUtils.getExtension(this.file.getName()); }

    @Override
    public String getBaseName() { return FilenameUtils.getBaseName(this.file.getName()); }

    @Override
    public String getLocation() { return this.file.getAbsolutePath(); }

    @Override
    public <T extends BaseFile>
    boolean equalsExtension(T file) { return this.getExtension().equals(file.getExtension()); }

    @Override
    public <T extends BaseFile>
    boolean equalsBaseName(T file) { return this.getBaseName().equals(file.getBaseName()); }

    @Override
    public <T extends BaseFile>
    boolean notEqualsExtension(T file) { return !equalsExtension(file); }

    @Override
    public <T extends BaseFile>
    boolean notEqualsBaseName(T file) { return !equalsBaseName(file); }

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
    public void deleteOnExit() { this.file.deleteOnExit(); }

    @Override
    public void rename(String fileName) throws HFileRenameFailedException {
        checkExists(this);
        final Path src = this.asPath();
        try {
            Files.move(src, src.resolveSibling(fileName));
        } catch (IOException io) {
            throw new HFileRenameFailedException(io, "File " + this.getLocation() + " has not been renamed");
        }
    }

    @Override
    public void rename(String name, String extension) throws HFileRenameFailedException {
        this.rename(name + extension);
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
        FileUtils.moveFile(this.file, file.file);
    }

    @Override
    public <T extends BaseDirectory>
    void moveToDirectory(T directory) throws IOException {
        checkExists(directory, this);
        FileUtils.moveFileToDirectory(file, directory.directory, false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public HDirectory getParent() throws ParentNotFoundException {
        checkExists(this);
        if (file.getAbsoluteFile().getParent() != null) {
            return new HDirectory(file.getAbsoluteFile().getParent());
        } else
            throw new ParentNotFoundException("The " + file.getAbsolutePath() + " file does not have a parent");

    }

    @Override
    public <T extends BaseDirectory> boolean isHierarchicalChild(T superParent) {
        checkExists(this, superParent);
        return isHierarchicalChild0(superParent);
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
        checkExists(this);
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException io) {
            throw new HFileReadException(io, "The reading of file " + this.getLocation() + " ended with an error");
        }
    }

    public HStream<String> readLines(Charset charset) throws HFileReadException {
        checkExists(this);
        try {
            return HStream.of(FileUtils.readLines(file, charset));
        } catch (IOException io) {
            throw new HFileReadException(io, "The reading of file " + this.getLocation() + " ended with an error");
        }
    }

    @Override
    public void writeLines(String... lines) throws IOException { writeLines(Arrays.asList(lines)); }

    public void writeLines(Collection<String> lines) throws HFileWriteException { writeLines(lines, false); }

    public void writeLines(Collection<String> lines, boolean append) throws HFileWriteException {
        checkExists(this);
        try {
            FileUtils.writeLines(file, lines, append);
        } catch (IOException io) {
            throw new HFileWriteException(io, "Writing to file " + this.getLocation() + " ended with an error");
        }
    }

    public long checksum() throws HFileReadException {
        checkExists(this);
        try {
            return FileUtils.checksumCRC32(file);
        } catch (IOException io) { throw new HFileReadException(io, "The checksum can't be received"); }
    }

    public InputStream openInputStream() throws HFileNotFoundException, HFileReadException {
        try {
            return FileUtils.openInputStream(file);
        } catch (FileNotFoundException fnf) {
            throw new HFileNotFoundException("File does not exist: " + file);
        } catch (IOException io) {
            throw new HFileReadException(io, "The reading of file " + this.getLocation() + " ended with an error");
        }
    }

    public OutputStream openOutputStream() throws IOException {
        return openOutputStream(false);
    }

    public OutputStream openOutputStream(boolean append) throws IOException {
        checkExists(this);
        return FileUtils.openOutputStream(file, append);
    }

    public double sizeOf(SizeType type) {
        return sizeOfAsBigDecimal(type).doubleValue();
    }

    @Override
    public BigDecimal sizeOfAsBigDecimal(SizeType type) {
        checkExists(this);
        BigDecimal size = new BigDecimal(FileUtils.sizeOfAsBigInteger(file));
        return SizeType.BYTE.to(type, size).setScale(1, RoundingMode.DOWN);
    }

    @Override
    public Path asPath() { return file.toPath(); }

    @Override
    public File asIOFile() { return file; }

    @Override
    public URI asURI() { return file.toURI(); }

    @Override
    public URL asURL() throws MalformedURLException { return file.toURI().toURL(); }

    public static HFile
    createTempFile(String fileName, FileAttribute<?>... attributes) throws IOException {
        return new HFile(Files.createTempFile(
                FilenameUtils.getBaseName(fileName),
                FilenameUtils.getExtension(fileName),
                attributes));
    }

    public static HFile
    createTempFile(HDirectory parent, String fileName, FileAttribute<?>... attributes) throws IOException {
        return new HFile(Files.createTempFile(
                parent.directory.toPath(),
                FilenameUtils.getBaseName(fileName),
                FilenameUtils.getExtension(fileName),
                attributes));
    }

    public static HFile
    createTempFile(String suffix, String prefix, FileAttribute<?>... attributes) throws IOException {
        return new HFile(Files.createTempFile(suffix, prefix, attributes));
    }

    public static HFile
    createTempFile(HDirectory parent, String suffix, String prefix, FileAttribute<?>... attributes) throws IOException {
        return new HFile(Files.createTempFile(parent.directory.toPath(), suffix, prefix, attributes));
    }

    public static HFile from(File file) { return new HFile(file.getPath()); }

    private boolean isHierarchicalChild0(BaseDirectory superParent) {
        File parent = file.getAbsoluteFile().getParentFile();
        while (parent != null) {
            if (parent.equals(superParent.directory)) {
                return true;
            } else parent = file.getAbsoluteFile().getParentFile();
        }

        return false;
    }

    private void checkExists(IFSObject... objects) {
        Objects.requireNonNull(objects, "Objects");
        for (IFSObject object : objects) {
            if (object.notExists()) {
                if (object instanceof BaseFile) {
                    throw new NoSuchHFileException("File does not exist: " + ((BaseFile) object).file);
                } else if (object instanceof BaseDirectory) {
                    throw new NoSuchHDirectoryException("Directory does not exist: " + ((BaseDirectory) object).directory);
                } else throw new IllegalArgumentException("IFSObjects don't inherit BaseFile or BaseDirectory!");
            }
        }
    }
}
