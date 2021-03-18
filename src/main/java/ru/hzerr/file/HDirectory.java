package ru.hzerr.file;

import org.apache.commons.io.FileUtils;
import ru.hzerr.collections.HCollectors;
import ru.hzerr.collections.list.ArrayHList;
import ru.hzerr.collections.list.HList;
import ru.hzerr.file.exception.ValidationException;
import ru.hzerr.file.exception.directory.HDirectoryCreateImpossibleException;
import ru.hzerr.file.exception.directory.HDirectoryIsNotDirectoryException;
import ru.hzerr.file.exception.directory.HDirectoryNotFoundException;
import ru.hzerr.file.exception.file.HFileCreateImpossibleException;
import ru.hzerr.file.exception.file.HFileCreationFailedException;
import ru.hzerr.file.exception.file.HFileIsNotFileException;
import ru.hzerr.stream.HStream;
import ru.hzerr.stream.bi.DoubleHStream;
import ru.hzerr.stream.bi.DoubleHStreamBuilder;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class HDirectory {

    File directory;

    public HDirectory(String pathname) {
        this.directory = new File(pathname);
        validate();
    }

    public HDirectory(String parent, String child) {
        this.directory = new File(parent, child);
        validate();
    }

    public HDirectory(URI uri) {
        this.directory = new File(uri);
        validate();
    }

    public HDirectory(HDirectory parent, String child) {
        this.directory = new File(parent.directory, child);
        validate();
    }

    public void create() throws HDirectoryIsNotDirectoryException, HDirectoryCreateImpossibleException {
        if (directory.exists()) {
            if (directory.isFile()) {
                throw new HDirectoryIsNotDirectoryException("File " + directory + " exists and is not a directory. Unable to create directory.");
            }
        } else if (!directory.mkdirs()) {
            // Double-check that some other thread or process hasn't made
            // the directory in the background
            if (directory.isFile()) {
                throw new HDirectoryCreateImpossibleException("Unable to create directory " + directory);
            }
        }
    }

    public String getDirectoryName() { return this.directory.getName(); }

    public HDirectory createSubDirectory(String dirName) throws HDirectoryIsNotDirectoryException, HDirectoryCreateImpossibleException {
        HDirectory subDirectory =  new HDirectory(this, dirName);
        subDirectory.create();
        return subDirectory;
    }

    public HFile createSubFile(String fileName) throws HFileIsNotFileException, HFileCreationFailedException, HFileCreateImpossibleException {
        HFile subFile = new HFile(this, fileName);
        subFile.create();
        return subFile;
    }

    public HDirectory getSubDirectory(String dirName) { return new HDirectory(this, dirName); }
    public HFile getSubFile(String fileName) { return new HFile(this, fileName); }

    public HStream<HFile> getFiles() {
        File[] files = this.directory.listFiles();
        if (files != null) {
            HList<HFile> subFiles = new ArrayHList<>();
            for (File file: files) {
                if (file.isFile()) {
                    subFiles.add(new HFile(file.getAbsolutePath()));
                }
            }
            return HStream.of(subFiles);
        } else return HStream.empty();
    }

    public HStream<HDirectory> getDirectories() {
        File[] files = this.directory.listFiles();
        if (files != null) {
            HList<HDirectory> subDirectories = new ArrayHList<>();
            for (File file: files) {
                if (file.isDirectory()) {
                    subDirectories.add(new HDirectory(file.getAbsolutePath()));
                }
            }
            return HStream.of(subDirectories);
        } else return HStream.empty();
    }

    public DoubleHStream<HDirectory, HFile> getFiles(boolean recursive) throws IOException {
        if (recursive) {
            HList<FSObject> objects = walk().map(FSObject::new).collect(HCollectors.toHList());
            HList<HDirectory> directories = new ArrayHList<>();
            HList<HFile> files = new ArrayHList<>();
            for (FSObject object : objects) {
                if (object.isDirectory()) {
                    directories.add(object.getDirectory());
                } else
                    files.add(object.getFile());
            }
            return DoubleHStreamBuilder.create(HDirectory.class, HFile.class)
                    .of(directories.toHStream(), files.toHStream());
        } else {
            HList<HDirectory> directories = new ArrayHList<>();
            HList<HFile> files = new ArrayHList<>();
            for (File object : directory.listFiles()) {
                if (object.isDirectory()) {
                    directories.add(new HDirectory(object.getAbsolutePath()));
                } else {
                    files.add(new HFile(object.getAbsolutePath()));
                }
            }
            return DoubleHStreamBuilder.create(HDirectory.class, HFile.class)
                    .of(directories.toHStream(), files.toHStream());
        }
    }

    public boolean exists() { return this.directory.exists(); }
    public boolean notExists() { return !this.directory.exists(); }

    public HDirectory getParent() { return new HDirectory(directory.getAbsoluteFile().getParent()); }

    public void cleanAll() throws IOException { FileUtils.cleanDirectory(directory); }
    public void clean(String name) throws IOException { FileUtils.forceDelete(new File(directory, name)); }
    public void delete() throws IOException { FileUtils.deleteDirectory(directory); }

    public void copyToDirectory(HDirectory directory) throws IOException {
        checkExists(this, directory);
        FileUtils.copyDirectoryToDirectory(this.directory, directory.directory);
    }

    public void copyContentToDirectory(HDirectory directory) throws IOException {
        checkExists(this, directory);
        FileUtils.copyDirectory(this.directory, directory.directory);
    }

    public void moveToDirectory(HDirectory directory) throws IOException {
        checkExists(this, directory);
        FileUtils.moveDirectoryToDirectory(this.directory, directory.directory, false);
    }

    public void moveContentToDirectory(HDirectory directory) throws IOException {
        checkExists(this, directory);
        FileUtils.moveDirectory(this.directory, directory.directory);
    }

    public double sizeOf(SizeType type) {
        BigDecimal size = new BigDecimal(FileUtils.sizeOfDirectoryAsBigInteger(directory));
        return SizeType.BYTE.to(type, size).setScale(1, RoundingMode.DOWN).doubleValue();
    }

    @Override
    public String toString() { return directory.getAbsolutePath(); }

    private void validate() throws ValidationException {
        if (directory.isFile())
            throw new ValidationException(directory + " is a file");
    }

    private void checkExists(HDirectory... directories) throws HDirectoryNotFoundException {
        Objects.requireNonNull(directories, "Directories");
        for (HDirectory directory : directories) {
            if (directory.notExists())
                throw new HDirectoryNotFoundException("Directory does not exist: " + directory);
        }
    }

    private HStream<Path> walk() throws IOException {
        return HStream.of(Files.walk(directory.toPath()));
    }
}
