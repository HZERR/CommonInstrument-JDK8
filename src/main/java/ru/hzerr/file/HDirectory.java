package ru.hzerr.file;

import org.apache.commons.io.FileUtils;
import ru.hzerr.file.annotation.MaybeRecursive;
import ru.hzerr.file.annotation.NotRecursive;
import ru.hzerr.file.annotation.Recursive;
import ru.hzerr.file.exception.directory.HDirectoryCreateImpossibleException;
import ru.hzerr.file.exception.directory.HDirectoryIsNotDirectoryException;
import ru.hzerr.file.exception.directory.NoSuchHDirectoryException;
import ru.hzerr.file.exception.file.HFileCreateImpossibleException;
import ru.hzerr.file.exception.file.HFileCreationFailedException;
import ru.hzerr.file.exception.file.HFileIsNotFileException;
import ru.hzerr.file.exception.file.NoSuchHFileException;
import ru.hzerr.file.stream.FileStream;
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
import java.util.regex.Pattern;

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
        HDirectory subDirectory = new HDirectory(this, dirName);
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
        return HStream.of(Files.list(directory.toPath()).spliterator())
                .filter(this::isNotDirectory)
                .map(HFile::new);
    }

    @Override
    @NotRecursive
    public HStream<HDirectory> getDirectories() throws IOException {
        checkExists(this);
        return HStream.of(Files.list(directory.toPath()).spliterator())
                .filter(Files::isDirectory)
                .map(HDirectory::new);
    }

    @Override
    @MaybeRecursive
    public FileStream getAllFiles(boolean recursive) throws IOException {
        checkExists(this);
        if (recursive) {
            return FileStream.of(walk());
        } else
            return FileStream.of(Files.list(directory.toPath()).spliterator());
    }

    @Override
    @MaybeRecursive
    public HStream<HFile> getFiles(boolean recursive) throws IOException {
        checkExists(this);
        if (recursive) {
            return HStream.of(Files.walk(directory.toPath()).spliterator())
                    .filter(path -> this.isNotDirectory(path) && !path.equals(directory.toPath()))
                    .map(HFile::new);
        } else
            return getFiles();
    }

    @Override
    @MaybeRecursive
    public HStream<HDirectory> getDirectories(boolean recursive) throws IOException {
        checkExists(this);
        if (recursive) {
            return HStream.of(Files.walk(directory.toPath()).spliterator())
                    .filter(path -> Files.isDirectory(path) && !path.equals(directory.toPath()))
                    .map(HDirectory::new);
        } else
            return getDirectories();
    }

    @Override
    @NotRecursive
    public <T extends BaseFile> HStream<HFile> getFilesExcept(T... filesToBeExcluded) throws IOException {
        checkExists(this);
        final HStream<BaseFile> excluded = HStream.of(filesToBeExcluded);
        return this.getFiles().filter(file -> excluded.noneMatch(file::equals));
    }

    /**
     * Пример входящих аргументов: java.exe, build.gradle
     * Не используйте имена без расширения файла
     */
    @Override
    @NotRecursive
    public <T extends BaseFile> HStream<HFile> getFilesExcept(String... fileNames) throws IOException {
        checkExists(this);
        final HStream<String> excludedFileNames = HStream.of(fileNames);
        return this.getFiles()
                .filter(file -> excludedFileNames.noneMatch(name -> file.getName().equals(name)));
    }

    @Override
    public <T extends BaseFile> HStream<HFile> getFilesExcept(boolean recursive, T... filesToBeExcluded) throws IOException {
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
    public <T extends BaseDirectory> HStream<HDirectory> getDirectoriesExcept(T... directoriesToBeExcluded) throws IOException {
        checkExists(this);
        final HStream<BaseDirectory> excludedStream = HStream.of(directoriesToBeExcluded);
        return this.getDirectories()
                .filter(dir -> excludedStream.noneMatch(dir::equals));
    }

    @Override
    @NotRecursive
    public <T extends BaseDirectory> HStream<HDirectory> getDirectoriesExcept(String... directoryNames) throws IOException {
        checkExists(this);
        final HStream<String> names = HStream.of(directoryNames);
        return this.getDirectories()
                .filter(dir -> names.noneMatch(name -> dir.getName().equals(name)));
    }

    @Override
    public <T extends BaseDirectory> HStream<HDirectory> getDirectoriesExcept(boolean recursive, T... directoriesToBeExcluded) throws IOException {
        checkExists(this);
        final HStream<T> filesToBeExcluded = HStream.of(directoriesToBeExcluded);
        if (recursive) {
            return HStream.of(Files.find(directory.toPath(), Integer.MAX_VALUE, (path, basicFileAttributes) -> {
                if (Files.isDirectory(path)) {
                    HDirectory dir = new HDirectory(path);
                    return filesToBeExcluded.allMatch(excludedDir -> {
                        return excludedDir.notIsHierarchicalChild(dir) && dir.notIsHierarchicalChild(excludedDir);
                    });
                } else
                    return false;
            }).spliterator()).map(HDirectory::new);
        } else
            return this.getDirectoriesExcept(directoriesToBeExcluded);
    }

    /**
     * Возвращает список всех файлов текущего каталога, кроме тех, которые должны быть исключены
     * @param filesToBeExcluded файлы, которые должны быть исключены
     * @param recursive использовать все внутренние файлы для перебора или нет
     * @return FileStream файлы, содержащиеся в текущем каталоге, кроме тех, которые были исключены
     * @throws IOException в случае ошибки ввода-вывода
     */
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
                }

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
                }
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
        return FileStream.of(Files.find(directory.toPath(), Integer.MAX_VALUE, ((path, basicFileAttributes) -> {
            return matcher.matches(path);
        })));
    }

    /**
     * Пример входящих аргументов метода: "java", "config", "persons"</br>
     * Не используйте имена с расширением файла
     */
    @Override
    public FileStream findByNames(String... names) throws IOException {
        checkExists(this);
        final HStream<String> includeNames = HStream.of(names);
        return this.getAllFiles(true)
                .filter(o -> includeNames.anyMatch(name -> o.getName().startsWith(name)));
    }

    @Override
    public HStream<? extends BaseDirectory> findDirectories(Predicate<? super BaseDirectory> matcher) throws IOException {
        checkExists(this);
        return this.getDirectories(true).filter(matcher::test);
    }

    @Override
    public HStream<? extends BaseDirectory> findDirectories(String glob) throws IOException {
        checkExists(this);
        if (glob.equals("*"))
            return this.getDirectories(true);
        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
        return HStream.of(Files.find(directory.toPath(), Integer.MAX_VALUE, ((path, basicFileAttributes) -> {
            return Files.isDirectory(path) && matcher.matches(path);
        })).spliterator()).map(HDirectory::new);
    }

    @Override
    public HStream<? extends BaseDirectory> findDirectoriesByNames(String... names) throws IOException {
        checkExists(this);
        final HStream<String> includeNames = HStream.of(names);
        return this.getDirectories(true)
                .filter(o -> includeNames.anyMatch(name -> o.getName().equals(name)));
    }

    @Override
    public HStream<? extends BaseFile> findFiles(Predicate<? super BaseFile> matcher) throws IOException {
        checkExists(this);
        return this.getFiles(true).filter(matcher::test);
    }

    @Override
    public HStream<? extends BaseFile> findFiles(String glob) throws IOException {
        checkExists(this);
        if (glob.equals("*"))
            return this.getFiles(true);
        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
        return HStream.of(Files.find(directory.toPath(), Integer.MAX_VALUE, ((path, basicFileAttributes) -> {
            return this.isNotDirectory(path) && matcher.matches(path);
        })).spliterator()).map(HFile::new);
    }

    /**
     * Пример входящих аргументов метода: "java", "config", "persons"</br>
     * Не используйте имена с расширением файла
     */
    @Override
    public HStream<? extends BaseFile> findFilesByNames(String... names) throws IOException {
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
    public HDirectory getParent() { return new HDirectory(directory.getAbsoluteFile().getParent()); }

    /**
     * Очищает полностью директорию без удаления текущего каталога
     */
    @Override
    public boolean clean() throws IOException {
        checkExists(this);
        FileUtils.cleanDirectory(directory);
        return getAllFiles(true).count() == 1;
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

    public static HDirectory createTempDirectory(String prefix, FileAttribute<?>... attributes) throws IOException {
        return new HDirectory(Files.createTempDirectory(prefix, attributes));
    }

    public static HDirectory createTempDirectory(HDirectory parent, String prefix, FileAttribute<?>... attributes) throws IOException {
        return new HDirectory(Files.createTempDirectory(parent.directory.toPath(), prefix, attributes));
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

    private boolean isNotDirectory(Path path) { return !Files.isDirectory(path); }

    private HStream<Path> walk() throws IOException {
        return HStream.of(Files.walk(directory.toPath()).spliterator())
                .filter(path -> !asPath().equals(path));
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
