package ru.hzerr.file;

import org.apache.commons.io.FileUtils;
import ru.hzerr.file.exception.directory.HDirectoryCreateImpossibleException;
import ru.hzerr.file.exception.directory.HDirectoryIsNotDirectoryException;
import ru.hzerr.file.exception.directory.NoSuchHDirectoryException;
import ru.hzerr.file.exception.file.HFileCreateImpossibleException;
import ru.hzerr.file.exception.file.HFileCreationFailedException;
import ru.hzerr.file.exception.file.HFileIsNotFileException;
import ru.hzerr.file.exception.file.NoSuchHFileException;
import ru.hzerr.file.stream.IFSObjects;
import ru.hzerr.stream.HStream;
import ru.hzerr.file.stream.FileStream;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@SuppressWarnings("unchecked")
public class HDirectory extends BaseDirectory {

    public HDirectory(URI uri) { super(uri); }
    public HDirectory(String pathname) { super(pathname); }
    public HDirectory(String parent, String child) { super(parent, child); }
    public HDirectory(HDirectory parent, String child) { super(parent, child); }
    HDirectory(Path path) { super(path.toString()); }

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
    public HDirectory getSubDirectory(String dirName) { return new HDirectory(this, dirName); }

    @Override
    public HFile getSubFile(String fileName) { return new HFile(this, fileName); }

    @Override
    @NotRecursive
    public HStream<HFile> getFiles() throws IOException {
        checkExists(this);
        return this.openDirectoryStream(path -> !Files.isDirectory(path), paths -> {
            return HStream.of(paths.spliterator()).map(HFile::new);
        });
    }

    @Override
    @NotRecursive
    public HStream<HDirectory> getDirectories() throws IOException {
        checkExists(this);
        return this.openDirectoryStream(Files::isDirectory, paths -> {
            return HStream.of(paths.spliterator()).map(HDirectory::new);
        });
    }

    @Override
    public FileStream getFiles(boolean recursive) throws IOException {
        checkExists(this);
        if (recursive) return FileStream.of(walk());
        return this.openDirectoryStream(paths -> {
            return FileStream.of(HStream.of(paths.spliterator()));
        });
    }


    @Override
    @NotRecursive
    public <T extends BaseFile> HStream<T> getFilesExcept(T... filesToBeExcluded) throws IOException {
        final HStream<BaseFile> excluded = HStream.of(filesToBeExcluded);
        final DirectoryStream.Filter<Path> FILE_FILTER = entry -> {
            return !Files.isDirectory(entry) && excluded.noneMatch(file -> new HFile(entry).equals(file));
        };
        return this.openDirectoryStream(FILE_FILTER, paths -> (HStream<T>) HStream.of(paths.spliterator()).map(HFile::new));
    }

    @Override
    public <T extends BaseFile> HStream<T> getFilesExcept(String... fileNames) throws IOException {
        final HStream<String> excludedFileNames = HStream.of(fileNames);
        final DirectoryStream.Filter<Path> FILE_FILTER = entry -> {
            return !Files.isDirectory(entry) && excludedFileNames.noneMatch(entry::endsWith);
        };
        return this.openDirectoryStream(FILE_FILTER, paths -> (HStream<T>) HStream.of(paths.spliterator()).map(HFile::new));
    }

    @Override
    @NotRecursive
    public <T extends BaseDirectory> HStream<T> getDirectoriesExcept(T... filesToBeExcluded) throws IOException {
        final HStream<BaseDirectory> excludedStream = HStream.of(filesToBeExcluded);
        final DirectoryStream.Filter<Path> DIRECTORY_FILTER = entry -> {
            if (Files.isDirectory(entry)) {
                return excludedStream.noneMatch(dir -> new HDirectory(entry).equals(dir));
            }

            return false;
        };
        
        return this.openDirectoryStream(DIRECTORY_FILTER, paths -> (HStream<T>) HStream.of(paths.spliterator()).map(HDirectory::new));
    }

    @Override
    @NotRecursive
    public <T extends BaseDirectory> HStream<T> getDirectoriesExcept(String... directoryNames) throws IOException {
        final HStream<String> names = HStream.of(directoryNames);
        final DirectoryStream.Filter<Path> DIRECTORY_FILTER = entry -> names.noneMatch(entry::endsWith) && Files.isDirectory(entry);
        return this.openDirectoryStream(DIRECTORY_FILTER, paths -> (HStream<T>) HStream.of(paths.spliterator()).map(HDirectory::new));
    }

    @Override
    public FileStream getFilesExcept(FileStream filesToBeExcluded, boolean recursive) throws IOException {
        final FileStream innerFiles = getFiles(recursive);
        return innerFiles.filter(innerFile -> filesToBeExcluded.noneMatch(excludedFile -> innerFile.getLocation().equals(excludedFile.getLocation())));
    }


    @Override
    public boolean isEmpty() {
        checkExists(this);
        final String[] fileNames = directory.list();
        // Double-check that some other thread or process has deleted
        // the directory in the background
        if (fileNames != null) {
            return fileNames.length == 0;
        } else throw new NoSuchHDirectoryException("Directory does not exist: " + directory);
    }

    @Override
    public boolean isNotEmpty() { return !isEmpty(); }

    @Override
    public boolean hasOnlyFiles() throws IOException {
        int fileCount = 0;
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory.toPath())) {
            while (paths.iterator().hasNext()) {
                Path path = paths.iterator().next();
                if (Files.isDirectory(path)) {
                    return false;
                } else
                    fileCount = fileCount + 1;
            }

            return fileCount > 0;
        }
    }

    @Override
    public boolean hasOnlyDirectories() throws IOException {
        int dirCount = 0;
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory.toPath())) {
            while (paths.iterator().hasNext()) {
                Path path = paths.iterator().next();
                if (Files.isDirectory(path)) {
                    dirCount = dirCount + 1;
                } else
                    return false;
            }

            return dirCount > 0;
        }
    }

    @Override
    public boolean isNotFoundInternalDirectories() throws IOException {
        checkExists(this);
        return this.getDirectories().count() == 0;
    }

    @Override
    public boolean isNotFoundInternalFiles() throws IOException {
        checkExists(this);
        return this.getFiles().count() == 0;
    }

    @Override
    public boolean contains(IFSObject object, boolean recursive) throws IOException {
        FileStream files = this.getFiles(recursive);
        return files.anyMatch(object::equals);
    }

    @Override
    public boolean exists() { return this.directory.exists(); }
    @Override
    public boolean notExists() { return !this.directory.exists(); }
    @Override
    public HDirectory getParent() { return new HDirectory(directory.getAbsoluteFile().getParent()); }

    /**
     * Cleans a directory without deleting it
     */
    @Override
    public boolean clean() throws IOException {
        checkExists(this);
        FileUtils.cleanDirectory(directory);
        return getFiles(true).count() == 1;
    }

    /**
     * case 1:
     *             DELETE        NO_DELETE-NO_DELETE-NO_DELETE
     *              TMP -> TMP2 -> TMP3 -> TMP4 -> TMP5.png
     *               C://Users//Desktop
     *
     * case 2:
     *           NO_DELETE        DELETE
     *              TMP -> TMP2 -> TMP3
     *               C://Users//Desktop
     * Удалить директорию, если исключаемая директория не является ребенком по отношению к данной директории
     * Удалить директорию, если данная директория не является ребенком по отношению к исключаемой директории
     */
    @Override
    public <T extends BaseDirectory> boolean deleteExcept(T... directories) throws IOException {
        checkExists(this);
        final HStream<BaseDirectory> excludedDirectories = HStream.of(directories);
        FileStream filteredStream = this.getFiles(true);
        if (filteredStream.count() > 32) filteredStream.parallel(IFSObjects.ALL);
        filteredStream
                .dirFilter(dir -> !dir.directory.equals(directory) && excludedDirectories.allMatch(excludedDir -> {
                    return excludedDir.notIsHierarchicalChild(dir) && dir.notIsHierarchicalChild(excludedDir);
                }))
                .fileFilter(file -> excludedDirectories.allMatch(file::notIsHierarchicalChild))
                .forEach(IFSObject::delete);
        return filteredStream.allMatch(IFSObject::notExists);
    }

    @Override
    public <T extends BaseFile> boolean deleteExcept(T... files) throws IOException {
        checkExists(this);
        final HStream<BaseFile> excludedFiles = HStream.of(files);
        FileStream filteredStream = this.getFiles(true);
        if (filteredStream.count() > 32) filteredStream.parallel(IFSObjects.ALL);
        filteredStream
                .dirFilter(dir -> !dir.directory.equals(directory) && excludedFiles.allMatch(excludedFile -> excludedFile.notIsHierarchicalChild(dir)))
                .fileFilter(file -> excludedFiles.allMatch(file::notEquals))
                .forEach(IFSObject::delete);
        return filteredStream.allMatch(IFSObject::notExists);
    }

    @Override
    @Recursive
    public boolean deleteExcept(FileStream excludedFiles) throws IOException {
        checkExists(this);
        FileStream filteredStream = this.getFiles(true);
        if (filteredStream.count() > 32) filteredStream.parallel(IFSObjects.ALL);
        filteredStream
                .dirFilter(dir -> !dir.directory.equals(directory) && excludedFiles.dirAllMatch(excludedDir -> {
                            return excludedDir.notIsHierarchicalChild(dir) && dir.notIsHierarchicalChild(excludedDir);
                        }) && excludedFiles.fileAllMatch(excludedFile -> excludedFile.notIsHierarchicalChild(dir)))
                .fileFilter(file -> excludedFiles.fileAllMatch(file::notEquals))
                .forEach(IFSObject::delete);
        return filteredStream.allMatch(IFSObject::notExists);
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

    @Override
    public Path asPath() { return directory.toPath(); }

    @Override
    public File asIOFile() { return directory; }

    @Override
    public URI asURI() { return directory.toURI(); }

    @Override
    public URL asURL() throws MalformedURLException { return directory.toURI().toURL(); }

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

    private void openDirectoryStream(Consumer<DirectoryStream<Path>> consumer) throws IOException {
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory.toPath())) {
            consumer.accept(paths);
        }
    }

    private void openDirectoryStream(DirectoryStream.Filter<Path> filter, Consumer<DirectoryStream<Path>> consumer) throws IOException {
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory.toPath(), filter)) {
            consumer.accept(paths);
        }
    }

    private <T> T openDirectoryStream(Function<DirectoryStream<Path>, T> func) throws IOException {
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory.toPath())) {
            return func.apply(paths);
        }
    }

    private <T> T openDirectoryStream(DirectoryStream.Filter<Path> filter, Function<DirectoryStream<Path>, T> func) throws IOException {
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory.toPath(), filter)) {
            return func.apply(paths);
        }
    }

    private static <T> HStream<T> asHStream(Iterator<T> sourceIterator) {
        return asHStream(sourceIterator, false);
    }

    private static <T> HStream<T> asHStream(Iterator<T> sourceIterator, boolean parallel) {
        Iterable<T> iterable = () -> sourceIterator;
        return HStream.of(StreamSupport.stream(iterable.spliterator(), parallel));
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
