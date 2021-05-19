package ru.hzerr.file;

import org.apache.commons.io.FileUtils;
import ru.hzerr.collections.HCollectors;
import ru.hzerr.collections.list.ArrayHList;
import ru.hzerr.collections.list.HList;
import ru.hzerr.file.exception.directory.HDirectoryCreateImpossibleException;
import ru.hzerr.file.exception.directory.HDirectoryIsNotDirectoryException;
import ru.hzerr.file.exception.directory.NoSuchHDirectoryException;
import ru.hzerr.file.exception.file.HFileCreateImpossibleException;
import ru.hzerr.file.exception.file.HFileCreationFailedException;
import ru.hzerr.file.exception.file.HFileIsNotFileException;
import ru.hzerr.file.exception.file.NoSuchHFileException;
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
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unchecked")
public class HDirectory extends BaseDirectory {

    public HDirectory(URI uri) { super(uri); }
    public HDirectory(String pathname) { super(pathname); }
    public HDirectory(String parent, String child) { super(parent, child); }
    public HDirectory(HDirectory parent, String child) { super(parent, child); }

    @Override
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

    @Override
    public String getName() { return this.directory.getName(); }

    @Override
    public String getLocation() { return this.directory.getAbsolutePath(); }

    @Override
    public HDirectory createSubDirectory(String dirName) throws HDirectoryIsNotDirectoryException, HDirectoryCreateImpossibleException {
        HDirectory subDirectory =  new HDirectory(this, dirName);
        subDirectory.create();
        return subDirectory;
    }

    @Override
    public HFile createSubFile(String fileName) throws HFileIsNotFileException, HFileCreationFailedException, HFileCreateImpossibleException {
        checkExists(this);
        HFile subFile = new HFile(this, fileName);
        subFile.create();
        return subFile;
    }

    @Override
    public HDirectory getSubDirectory(String dirName) {
        checkExists(this);
        return new HDirectory(this, dirName);
    }

    @Override
    public HFile getSubFile(String fileName) {
        checkExists(this);
        return new HFile(this, fileName);
    }

    @Override
    public HStream<HFile> getFiles() {
        checkExists(this);
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

    @Override
    public HStream<HDirectory> getDirectories() {
        checkExists(this);
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

    @Override
    public DoubleHStream<HDirectory, HFile> getFiles(boolean recursive) throws IOException {
        checkExists(this);
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
        }
        HList<HDirectory> directories = new ArrayHList<>();
        HList<HFile> files = new ArrayHList<>();
        File[] objects = directory.listFiles();
        if (objects != null) {
            for (File object : objects) {
                if (object.isDirectory()) {
                    directories.add(new HDirectory(object.getAbsolutePath()));
                } else
                    files.add(new HFile(object.getAbsolutePath()));
            }
        }
        return DoubleHStreamBuilder.create(HDirectory.class, HFile.class)
                .of(directories.toHStream(), files.toHStream());
    }

    @Override
    public boolean exists() { return this.directory.exists(); }
    @Override
    public boolean notExists() { return !this.directory.exists(); }
    @Override
    public HDirectory getParent() { return new HDirectory(directory.getAbsoluteFile().getParent()); }

    @Override
    public boolean clean() throws IOException {
        checkExists(this);
        FileUtils.cleanDirectory(directory);
        return getFiles(true).count(Object.class) == 1;
    }

    @Override
    public <T extends BaseDirectory> boolean deleteExcept(T... directories) throws IOException {
        checkExists(this);
        checkExists(directories);
        HStream<BaseDirectory> excludedFiles = HStream.of(directories);
        DoubleHStream<HDirectory, HFile> filteredStream = this.getFiles(true);
        if (filteredStream.count(Object.class) > 32) filteredStream.parallel(Object.class);
        filteredStream.filter(HDirectory.class, dir -> !dir.directory.equals(directory))
                .filter(HDirectory.class, directory -> excludedFiles.allMatch(directory::notIsHierarchicalChild))
                .forEach(HDirectory.class, HDirectory::delete);
        return filteredStream.allMatch(HDirectory.class, HDirectory::notExists);
    }

    @Override
    public <T extends BaseFile> boolean deleteExcept(T... files) throws IOException {
        return deleteExcept(DoubleHStreamBuilder.create(HDirectory.class, HFile.class).of(HStream.empty(), HStream.of((HFile[]) files)));
    }

    @Override
    public <ID extends BaseDirectory, IF extends BaseFile>
    boolean deleteExcept(DoubleHStream<ID, IF> excludedFiles) throws IOException {
        checkExists(this);
        DoubleHStream<HDirectory, HFile> filteredStream = this.getFiles(true);
        if (filteredStream.count(Object.class) > 32) filteredStream.parallel(Object.class);
        filteredStream.filter(HDirectory.class, dir -> !dir.directory.equals(directory))
                .filter(HFile.class, file -> excludedFiles.allMatch(HFile.class, internalFile -> !internalFile.equals(file)))
                .filter(HDirectory.class, directory -> excludedFiles.allMatch(HDirectory.class, directory::notIsHierarchicalChild))
                .filter(HFile.class, file -> excludedFiles.allMatch(HDirectory.class, file::notIsHierarchicalChild))
                .forEach(HDirectory.class, HDirectory::delete)
                .forEach(HFile.class, HFile::delete);
        boolean dirDeleted = filteredStream.allMatch(HDirectory.class, HDirectory::notExists);
        boolean fileDeleted = filteredStream.allMatch(HFile.class, HFile::notExists);
        return dirDeleted && fileDeleted;
    }

    @Override
    public boolean delete(String name) throws IOException {
        checkExists(this);
        File resource = new File(directory, name);
        if (!resource.exists()) throw new NoSuchHFileException("File " + resource + " not found");
        FileUtils.forceDelete(resource);
        return !resource.exists();
    }

    @Override
    public boolean delete() throws IOException {
        checkExists(this);
        FileUtils.deleteDirectory(directory);
        return notExists();
    }

    @Override
    public <T extends BaseDirectory> boolean isHierarchicalChild(T superParent) {
        checkExists(this, superParent);
        try {
            return isHierarchicalChild0(superParent);
        } catch (NullPointerException npe) { return false; }
    }

    @Override
    public <T extends BaseDirectory> boolean notIsHierarchicalChild(T superParent) {
        return !isHierarchicalChild(superParent);
    }

    @Override
    public <T extends BaseDirectory> void copyToDirectory(T directory) throws IOException {
        checkExists(this, directory);
        FileUtils.copyDirectoryToDirectory(this.directory, directory.directory);
    }

    public <T extends BaseDirectory> void copyContentToDirectory(T directory) throws IOException {
        checkExists(this, directory);
        FileUtils.copyDirectory(this.directory, directory.directory);
    }

    public <T extends BaseDirectory> void moveToDirectory(T directory) throws IOException {
        checkExists(this, directory);
        FileUtils.moveDirectoryToDirectory(this.directory, directory.directory, false);
    }

    public <T extends BaseDirectory> void moveContentToDirectory(T directory) throws IOException {
        checkExists(this, directory);
        FileUtils.moveDirectory(this.directory, directory.directory);
    }

    public double sizeOf(SizeType type) {
        checkExists(this);
        BigDecimal size = new BigDecimal(FileUtils.sizeOfDirectoryAsBigInteger(directory));
        return SizeType.BYTE.to(type, size).setScale(1, RoundingMode.DOWN).doubleValue();
    }

    private void checkExists(BaseDirectory... directories) {
        Objects.requireNonNull(directories, "Directories");
        for (BaseDirectory directory : directories) {
            if (directory.notExists())
                throw new NoSuchHDirectoryException("Directory does not exist: " + directory);
        }
    }

    private HStream<Path> walk() throws IOException {
        return HStream.of(Files.walk(directory.toPath()));
    }

    private boolean isHierarchicalChild0(BaseDirectory superParent) {
        if (this.directory.equals(superParent.directory)) return true;
        BaseDirectory supDirectory = getParent();
        while (supDirectory != null) {
            if (supDirectory.directory.equals(superParent.directory)) {
                return true;
            } else supDirectory = supDirectory.getParent();
        }

        return false;
    }
}
