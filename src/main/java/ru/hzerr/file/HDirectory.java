package ru.hzerr.file;

import org.apache.commons.io.FileUtils;
import ru.hzerr.file.annotation.MaybeRecursive;
import ru.hzerr.file.annotation.NotRecursive;
import ru.hzerr.file.annotation.Recursive;
import ru.hzerr.file.exception.ParentNotFoundException;
import ru.hzerr.file.exception.directory.*;
import ru.hzerr.file.exception.file.HFileCreateImpossibleException;
import ru.hzerr.file.exception.file.HFileCreationFailedException;
import ru.hzerr.file.exception.file.HFileIsNotFileException;
import ru.hzerr.file.exception.file.NoSuchHFileException;
import ru.hzerr.file.stream.FileStream;
import ru.hzerr.file.stream.FileStreamBuilder;
import ru.hzerr.stream.HStream;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"unchecked", "CodeBlock2Expr"})
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
    public BaseDirectory createSubDirectory(String dirName) throws HDirectoryIsNotDirectoryException, HDirectoryCreateImpossibleException {
        HDirectory subDirectory = new HDirectory(this, dirName);
        subDirectory.create();
        return subDirectory;
    }

    @Override
    public BaseFile createSubFile(String fileName) throws HFileIsNotFileException, HFileCreationFailedException, HFileCreateImpossibleException {
        checkExists(this);
        HFile subFile = new HFile(this, fileName);
        subFile.create();
        return subFile;
    }

    @Override
    public BaseDirectory getSubDirectory(String dirName) { return new HDirectory(this, dirName); }

    @Override
    public BaseFile getSubFile(String fileName) { return new HFile(this, fileName); }

    @Override
    public void rename(String fullName) throws HDirectoryRenameFailedException, HDirectoryRenameImpossibleException {
        if (directory.getAbsoluteFile().getParent() != null) {
            File dir = new File(Paths.get(directory.getAbsoluteFile().getParent()).resolve(fullName).normalize().toString());
            try {
                FileUtils.copyDirectory(directory, dir);
                delete();
                directory = dir;
            } catch (IOException io) {
                throw new HDirectoryRenameFailedException(io, "Directory " + this.getLocation() + " has not been renamed");
            }
        } else throw new HDirectoryRenameImpossibleException("The directory does not have a parent directory");
    }

    @Override
    @NotRecursive
    public HStream<BaseFile> getFiles() throws IOException {
        checkExists(this);
        try(Stream<Path> pathStream = Files.list(directory.toPath())) {
            return HStream.of(pathStream.collect(Collectors.toList()))
                .filter(this::isNotDirectory)
                    .map(HFile::new);
        }
    }

    @Override
    @NotRecursive
    public HStream<BaseDirectory> getDirectories() throws IOException {
        checkExists(this);
        try(Stream<Path> pathStream = Files.list(directory.toPath())) {
            return HStream.of(pathStream.collect(Collectors.toList()))
                    .filter(Files::isDirectory)
                    .map(HDirectory::new);
        }
    }

    @Override
    @MaybeRecursive
    public FileStream getAllFiles(boolean recursive) throws IOException {
        checkExists(this);
        if (recursive) {
            return FileStream.of(walk());
        } else try (Stream<Path> pathStream = Files.list(directory.toPath())) {
            return FileStreamBuilder.newBuilder().add(pathStream.collect(Collectors.toList())).newStream();
        }
    }

    @Override
    @MaybeRecursive
    public HStream<BaseFile> getFiles(boolean recursive) throws IOException {
        checkExists(this);
        if (recursive) {
            try (Stream<Path> pathStream = Files.walk(directory.toPath())) {
                return HStream.of(pathStream.collect(Collectors.toList()))
                        .filter(path -> isNotDirectory(path) && !path.equals(directory.toPath()))
                        .map(HFile::new);
            }
        } else
            return getFiles();
    }

    @Override
    @MaybeRecursive
    public HStream<BaseDirectory> getDirectories(boolean recursive) throws IOException {
        checkExists(this);
        if (recursive) {
            try (Stream<Path> pathStream = Files.walk(directory.toPath())) {
                return HStream.of(pathStream.collect(Collectors.toList()))
                        .filter(path -> Files.isDirectory(path) && !path.equals(directory.toPath()))
                        .map(HDirectory::new);
            }
        } else
            return getDirectories();
    }

    @Override
    @NotRecursive
    public <T extends BaseFile> HStream<BaseFile> getFilesExcept(T... filesToBeExcluded) throws IOException {
        checkExists(this);
        final HStream<BaseFile> excluded = HStream.of(filesToBeExcluded);
        return this.getFiles().filter(file -> excluded.noneMatch(file::equals));
    }

    @Override
    @NotRecursive
    public <T extends BaseFile> HStream<BaseFile> getFilesExcept(String... fileNames) throws IOException {
        checkExists(this);
        final HStream<String> excludedFileNames = HStream.of(fileNames);
        return this.getFiles()
                .filter(file -> excludedFileNames.noneMatch(name -> file.getName().equals(name)));
    }

    @Override
    public <T extends BaseFile> HStream<BaseFile> getFilesExcept(boolean recursive, T... filesToBeExcluded) throws IOException {
        checkExists(this);
        final HStream<BaseFile> excludedFiles = HStream.of(filesToBeExcluded);
        if (recursive) {
            return this.getFiles(true)
                    .filter(file -> excludedFiles.noneMatch(file::equals));
        } else
            return this.getFilesExcept(filesToBeExcluded);
    }

    @Override
    @NotRecursive
    public <T extends BaseDirectory> HStream<BaseDirectory> getDirectoriesExcept(T... directoriesToBeExcluded) throws IOException {
        checkExists(this);
        final HStream<BaseDirectory> excludedStream = HStream.of(directoriesToBeExcluded);
        return this.getDirectories()
                .filter(dir -> excludedStream.noneMatch(dir::equals));
    }

    @Override
    @NotRecursive
    public <T extends BaseDirectory> HStream<BaseDirectory> getDirectoriesExcept(String... directoryNames) throws IOException {
        checkExists(this);
        final HStream<String> names = HStream.of(directoryNames);
        return this.getDirectories()
                .filter(dir -> names.noneMatch(name -> dir.getName().equals(name)));
    }

    @Override
    public <T extends BaseDirectory> HStream<BaseDirectory> getDirectoriesExcept(boolean recursive, T... directoriesToBeExcluded) throws IOException {
        checkExists(this);
        final HStream<T> filesToBeExcluded = HStream.of(directoriesToBeExcluded);
        if (recursive) {
            try(Stream<Path> pathStream = Files.find(directory.toPath(), Integer.MAX_VALUE, (path, basicFileAttributes) -> {
                if (Files.isDirectory(path)) {
                    HDirectory dir = new HDirectory(path);
                    return filesToBeExcluded.allMatch(excludedDir -> {
                        return excludedDir.notIsHierarchicalChild(dir) && dir.notIsHierarchicalChild(excludedDir);
                    });
                } else
                    return false;
            })) {
                return HStream.of(pathStream.collect(Collectors.toList())).map(HDirectory::new);
            }
        } else
            return this.getDirectoriesExcept(directoriesToBeExcluded);
    }

    @Override
    @MaybeRecursive
    public FileStream getAllFilesExcept(FileStream filesToBeExcluded, boolean recursive) throws IOException {
        checkExists(this);
        if (recursive) {
            FileStream filteredStream = this.getAllFiles(true);
            return filteredStream
                    .dirFilter(dir -> filesToBeExcluded.dirAllMatch(excludedDir -> {
                        return excludedDir.notIsHierarchicalChild(dir) && dir.notIsHierarchicalChild(excludedDir);
                    }) && filesToBeExcluded.fileAllMatch(excludedFile -> excludedFile.notIsHierarchicalChild(dir)))
                    .fileFilter(file -> filesToBeExcluded.fileAllMatch(file::notEquals));
        } else
            return FileStream.of(walk()).filter(o -> filesToBeExcluded.noneMatch(o::equals));
    }

    @Override
    public boolean isEmpty() {
        checkExists(this);
        final String[] fileNames = directory.list();
        // Double-check that some other thread or process has deleted
        // the directory in the background
        if (fileNames != null) {
            return fileNames.length == 0;
        } else
            throw new NoSuchHDirectoryException("Directory does not exist: " + directory);
    }

    @Override
    public boolean isNotEmpty() { return !isEmpty(); }

    @Override
    public boolean hasOnlyFiles() throws IOException {
        checkExists(this);
        int fileCount = 0;
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory.toPath())) {
            while (paths.iterator().hasNext()) {
                Path path = paths.iterator().next();
                if (this.isNotDirectory(path)) {
                    fileCount = fileCount + 1;
                } else
                    return false;
            }

            return fileCount > 0;
        }
    }

    @Override
    public boolean hasOnlyDirectories() throws IOException {
        checkExists(this);
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
    public boolean notFoundInternalDirectories() throws IOException {
        checkExists(this);
        return this.getDirectories().count() == 0;
    }

    @Override
    public boolean notFoundInternalFiles() throws IOException {
        checkExists(this);
        return this.getFiles().count() == 0;
    }

    @Override
    public boolean contains(IFSObject object, boolean recursive) throws IOException {
        checkExists(this);
        FileStream files = this.getAllFiles(recursive);
        return files.anyMatch(object::equals);
    }

    @Override
    public FileStream find(Predicate<? super IFSObject> matcher) throws IOException {
        checkExists(this);
        return this.getAllFiles(true).filter(matcher::test);
    }

    @Override
    public FileStream find(String glob) throws IOException {
        checkExists(this);
        if (glob.equals("*"))
            return this.getAllFiles(true);
        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
        try (Stream<Path> pathStream = Files.find(directory.toPath(), Integer.MAX_VALUE, ((path, basicFileAttributes) -> {
            return matcher.matches(path);
        }))) {
            return FileStreamBuilder.newBuilder().add(pathStream.collect(Collectors.toList())).newStream();
        }
    }

    @Override
    public FileStream findByNames(String... names) throws IOException {
        checkExists(this);
        final HStream<String> includeNames = HStream.of(names);
        return this.getAllFiles(true)
                .filter(o -> includeNames.anyMatch(name -> o.getName().startsWith(name)));
    }

    @Override
    public HStream<BaseDirectory> findDirectories(Predicate<? super BaseDirectory> matcher) throws IOException {
        checkExists(this);
        return this.getDirectories(true).filter(matcher::test);
    }

    @Override
    public HStream<BaseDirectory> findDirectories(String glob) throws IOException {
        checkExists(this);
        if (glob.equals("*"))
            return this.getDirectories(true);
        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
        try (Stream<Path> pathStream = Files.find(directory.toPath(), Integer.MAX_VALUE, ((path, basicFileAttributes) -> {
            return Files.isDirectory(path) && matcher.matches(path);
        }))) {
            return HStream.of(pathStream.collect(Collectors.toList())).map(HDirectory::new);
        }
    }

    @Override
    public HStream<BaseDirectory> findDirectoriesByNames(String... names) throws IOException {
        checkExists(this);
        final HStream<String> includeNames = HStream.of(names);
        return this.getDirectories(true)
                .filter(o -> includeNames.anyMatch(name -> o.getName().equals(name)));
    }

    @Override
    public HStream<BaseFile> findFiles(Predicate<? super BaseFile> matcher) throws IOException {
        checkExists(this);
        return this.getFiles(true).filter(matcher::test);
    }

    @Override
    public HStream<BaseFile> findFiles(String glob) throws IOException {
        checkExists(this);
        if (glob.equals("*"))
            return this.getFiles(true);
        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
        try (Stream<Path> pathStream = Files.find(directory.toPath(), Integer.MAX_VALUE, ((path, basicFileAttributes) -> {
            return this.isNotDirectory(path) && matcher.matches(path);
        }))) {
            return HStream.of(pathStream.collect(Collectors.toList())).map(HFile::new);
        }
    }

    @Override
    public HStream<BaseFile> findFilesByNames(String... names) throws IOException {
        checkExists(this);
        final HStream<String> includeNames = HStream.of(names);
        return this.getFiles(true)
                .filter(o -> includeNames.anyMatch(name -> o.getName().startsWith(name)));
    }

    @Override
    public boolean exists() { return this.directory.exists(); }
    @Override
    public boolean notExists() { return !this.directory.exists(); }
    @Override
    public HDirectory getParent() throws ParentNotFoundException {
        if (directory.getAbsoluteFile().getParent() != null) {
            return new HDirectory(directory.getAbsoluteFile().getParent());
        } else
            throw new ParentNotFoundException("The " + directory.getAbsolutePath() + " directory does not have a parent");
    }

    @Override
    public void clean() throws IOException {
        checkExists(this);
        FileUtils.cleanDirectory(directory);
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
    @Recursive
    public <T extends BaseDirectory> boolean deleteExcept(T... directories) throws IOException {
        checkExists(this);
        final HStream<BaseDirectory> excludedDirectories = HStream.of(directories);
        FileStream filteredStream = this.getAllFiles(true);
        filteredStream.parallelIfNeeded();
        filteredStream
                .dirFilter(dir -> !dir.directory.equals(directory) && excludedDirectories.allMatch(excludedDir -> {
                    return excludedDir.notIsHierarchicalChild(dir) && dir.notIsHierarchicalChild(excludedDir);
                }))
                .fileFilter(file -> excludedDirectories.allMatch(file::notIsHierarchicalChild))
                .forEach(IFSObject::delete);
        return filteredStream.allMatch(IFSObject::notExists);
    }

    @Override
    @Recursive
    public <T extends BaseFile> boolean deleteExcept(T... files) throws IOException {
        checkExists(this);
        final HStream<BaseFile> excludedFiles = HStream.of(files);
        FileStream filteredStream = this.getAllFiles(true);
        filteredStream.parallelIfNeeded();
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
        FileStream filteredStream = this.getAllFiles(true);
        filteredStream.parallelIfNeeded();
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
    public void deleteOnExit() { this.directory.deleteOnExit(); }

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
        this.moveContentToDirectory(directory, false);
    }

    public <T extends BaseDirectory> void moveContentToDirectory(T directory, boolean shouldDeleteDirToBeMoved) throws IOException {
        checkExists(this, directory);
        FileUtils.copyDirectory(this.directory, directory.directory);
        if (shouldDeleteDirToBeMoved) {
            delete();
        } else
            clean();
    }

    public double sizeOf(SizeType type) {
        return sizeOfAsBigDecimal(type).doubleValue();
    }

    public BigDecimal sizeOfAsBigDecimal(SizeType type) {
        checkExists(this);
        BigDecimal size = new BigDecimal(FileUtils.sizeOfDirectoryAsBigInteger(directory));
        return SizeType.BYTE.to(type, size).setScale(1, RoundingMode.DOWN);
    }

    @Override
    public Path asPath() { return directory.toPath(); }

    @Override
    public File asIOFile() { return directory; }

    @Override
    public URI asURI() { return directory.toURI(); }

    @Override
    public URL asURL() throws MalformedURLException { return directory.toURI().toURL(); }

    public static HDirectory createTempDirectory(String prefix, FileAttribute<?>... attributes) throws IOException {
        return new HDirectory(Files.createTempDirectory(prefix, attributes));
    }

    public static HDirectory createTempDirectory(HDirectory parent, String prefix, FileAttribute<?>... attributes) throws IOException {
        return new HDirectory(Files.createTempDirectory(parent.directory.toPath(), prefix, attributes));
    }

    public static HDirectory from(File dir) { return new HDirectory(dir.toPath()); }

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

    private boolean isNotDirectory(Path path) { return !Files.isDirectory(path); }

    private HStream<Path> walk() throws IOException {
        try (Stream<Path> pathStream = Files.walk(directory.toPath())) {
            return HStream.of(pathStream.collect(Collectors.toList()))
                    .filter(path -> !asPath().equals(path));
        }
    }

    private boolean isHierarchicalChild0(BaseDirectory superParent) {
        if (this.directory.equals(superParent.directory)) return true;
        File supDirectory = directory.getAbsoluteFile().getParentFile();
        while (supDirectory != null) {
            if (supDirectory.equals(superParent.directory)) {
                return true;
            } else supDirectory = directory.getAbsoluteFile().getParentFile();
        }

        return false;
    }
}
