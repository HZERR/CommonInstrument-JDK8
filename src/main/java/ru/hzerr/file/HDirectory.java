package ru.hzerr.file;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import ru.hzerr.collections.list.ArrayHList;
import ru.hzerr.collections.list.HList;
import ru.hzerr.file.annotation.MaybeRecursive;
import ru.hzerr.file.annotation.NotRecursive;
import ru.hzerr.file.annotation.Recursive;
import ru.hzerr.file.exception.ParentNotFoundException;
import ru.hzerr.file.exception.directory.*;
import ru.hzerr.file.exception.file.HFileCreateImpossibleException;
import ru.hzerr.file.exception.file.HFileIsNotFileException;
import ru.hzerr.file.exception.file.NoSuchHFileException;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings({"unchecked", "CodeBlock2Expr"})
public class HDirectory extends BaseDirectory {

    public HDirectory(URI uri) { super(uri); }
    public HDirectory(String pathname) { super(pathname); }
    public HDirectory(String parent, String child) { super(parent, child); }
    public HDirectory(BaseDirectory parent, String child) { super(parent, child); }
    protected HDirectory(Path path) { super(path.toString()); }

    @Override
    public void create() throws HDirectoryCreateImpossibleException {
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
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
    public BaseFile createSubFile(String fileName) throws HFileIsNotFileException, HFileCreateImpossibleException {
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
    public HList<BaseFile> getFiles() throws IOException {
        checkExists(this);
        try(Stream<Path> pathStream = Files.list(directory.toPath())) {
            return pathStream
                    .parallel()
                    .filter(this::isNotDirectory)
                    .map(HFile::new)
                    .collect(Collectors.toCollection(ArrayHList::new));
        }
    }

    @Override
    @NotRecursive
    public HList<BaseDirectory> getDirectories() throws IOException {
        checkExists(this);
        try(Stream<Path> pathStream = Files.list(directory.toPath())) {
            return pathStream
                    .parallel()
                    .filter(Files::isDirectory)
                    .map(HDirectory::new)
                    .collect(Collectors.toCollection(ArrayHList::new));
        }
    }

    @Override
    @MaybeRecursive
    public HList<IFSObject> getAllFiles(boolean recursive) throws IOException {
        checkExists(this);
        if (recursive) {
            try (Stream<Path> pathStream = Files.walk(directory.toPath())) {
                return pathStream
                        .parallel()
                        .filter(path -> !directory.toPath().equals(path))
                        .map(path -> {
                            return Files.isDirectory(path) ? new HDirectory(path) : new HFile(path);
                        })
                        .collect(Collectors.toCollection(ArrayHList::new));
            }
        } else try (Stream<Path> pathStream = Files.list(directory.toPath())) {
            return pathStream
                    .parallel()
                    .map(path -> {
                        return Files.isDirectory(path) ? new HDirectory(path) : new HFile(path);
                    })
                    .collect(Collectors.toCollection(ArrayHList::new));
        }
    }

    @Override
    @MaybeRecursive
    public HList<BaseFile> getFiles(boolean recursive) throws IOException {
        checkExists(this);
        if (recursive) {
            try (Stream<Path> pathStream = Files.walk(directory.toPath())) {
                return pathStream
                        .parallel()
                        .filter(this::isNotDirectory)
                        .map(HFile::new)
                        .collect(Collectors.toCollection(ArrayHList::new));
            }
        } else
            return getFiles();
    }

    @Override
    @MaybeRecursive
    public HList<BaseDirectory> getDirectories(boolean recursive) throws IOException {
        checkExists(this);
        if (recursive) {
            try (Stream<Path> pathStream = Files.walk(directory.toPath())) {
                return pathStream
                        .parallel()
                        .filter(path -> Files.isDirectory(path) && !path.equals(directory.toPath()))
                        .map(HDirectory::new)
                        .collect(Collectors.toCollection(ArrayHList::new));
            }
        } else
            return getDirectories();
    }

    @Override
    @NotRecursive
    public <T extends BaseFile> HList<BaseFile> getFilesExcept(T... filesToBeExcluded) throws IOException {
        checkExists(this);
        final HList<BaseFile> excluded = HList.of(filesToBeExcluded);
        try(Stream<Path> pathStream = Files.list(directory.toPath())) {
            return pathStream
                    .parallel()
                    .filter(path -> {
                        return isNotDirectory(path) && excluded.noneMatch(baseFile -> baseFile.asPath().equals(path));
                    })
                    .map(HFile::new)
                    .collect(Collectors.toCollection(ArrayHList::new));
        }
    }

    @Override
    @NotRecursive
    public <T extends BaseFile> HList<BaseFile> getFilesExcept(String... fileNames) throws IOException {
        checkExists(this);
        final HList<String> excludedFileNames = HList.of(fileNames);
        try(Stream<Path> pathStream = Files.list(directory.toPath())) {
            return pathStream
                    .parallel()
                    .filter(path -> {
                        return isNotDirectory(path) && excludedFileNames.noneMatch(fileName -> FilenameUtils.getName(path.toString()).equals(fileName));
                    })
                    .map(HFile::new)
                    .collect(Collectors.toCollection(ArrayHList::new));
        }
    }

    @Override
    @MaybeRecursive
    public <T extends BaseFile> HList<BaseFile> getFilesExcept(boolean recursive, T... filesToBeExcluded) throws IOException {
        checkExists(this);
        final HList<BaseFile> excludedFiles = HList.of(filesToBeExcluded);
        if (recursive) {
            try (Stream<Path> pathStream = Files.walk(directory.toPath())) {
                return pathStream
                        .parallel()
                        .filter(path -> isNotDirectory(path) && excludedFiles.noneMatch(file -> file.asPath().equals(path)))
                        .map(HFile::new)
                        .collect(Collectors.toCollection(ArrayHList::new));
            }
        } else
            return this.getFilesExcept(filesToBeExcluded);
    }

    @Override
    @NotRecursive
    public <T extends BaseDirectory> HList<BaseDirectory> getDirectoriesExcept(T... directoriesToBeExcluded) throws IOException {
        checkExists(this);
        final HList<BaseDirectory> excludedStream = HList.of(directoriesToBeExcluded);
        try (Stream<Path> pathStream = Files.list(directory.toPath())) {
            return pathStream
                    .parallel()
                    .filter(path -> Files.isDirectory(path) && !path.equals(directory.toPath()) && excludedStream.noneMatch(dir -> dir.asPath().equals(path)))
                    .map(HDirectory::new)
                    .collect(Collectors.toCollection(ArrayHList::new));
        }
    }

    @Override
    @NotRecursive
    public <T extends BaseDirectory> HList<BaseDirectory> getDirectoriesExcept(String... directoryNames) throws IOException {
        checkExists(this);
        final HList<String> names = HList.of(directoryNames);
        try(Stream<Path> pathStream = Files.list(directory.toPath())) {
            return pathStream
                    .parallel()
                    .filter(path -> Files.isDirectory(path) && names.noneMatch(name -> FilenameUtils.getName(path.toString()).equals(name)))
                    .map(HDirectory::new)
                    .collect(Collectors.toCollection(ArrayHList::new));
        }
    }

    @Override
    @MaybeRecursive
    public <T extends BaseDirectory> HList<BaseDirectory> getDirectoriesExcept(boolean recursive, T... directoriesToBeExcluded) throws IOException {
        checkExists(this);
        final HList<T> filesToBeExcluded = HList.of(directoriesToBeExcluded);
        if (recursive) {
            try (Stream<Path> pathStream = Files.walk(directory.toPath())) {
                return pathStream
                        .parallel()
                        .filter(path -> Files.isDirectory(path) && filesToBeExcluded.allMatch(excludedDir -> {
                            HDirectory dir = new HDirectory(path);
                            return excludedDir.notIsHierarchicalChild(dir) && dir.notIsHierarchicalChild(excludedDir);
                        }))
                        .map(HDirectory::new)
                        .collect(Collectors.toCollection(ArrayHList::new));
            }
        } else
            return this.getDirectoriesExcept(directoriesToBeExcluded);
    }

    @Override
    @MaybeRecursive
    @SuppressWarnings("ConstantConditions")
    public HList<IFSObject> getAllFilesExcept(HList<IFSObject> filesToBeExcluded, boolean recursive) throws IOException {
        checkExists(this);
        if (recursive) {
            try (Stream<Path> pathStream = Files.walk(directory.toPath())) {
                return pathStream
                        .parallel()
                        .map(path -> {
                            return Files.isDirectory(path) ? new HDirectory(path) : new HFile(path);
                        })
                        .filter(ifsObject -> {
                            if (Files.isDirectory(ifsObject.asPath())) {
                                return !directory.toPath().equals(ifsObject.asPath()) && filesToBeExcluded.allMatch(excludedFSObj -> {
                                    if (Files.isDirectory(excludedFSObj.asPath())) {
                                        return excludedFSObj.notIsHierarchicalChild((HDirectory) ifsObject) && ifsObject.notIsHierarchicalChild((HDirectory) excludedFSObj);
                                    } else
                                        return excludedFSObj.notIsHierarchicalChild((HDirectory) ifsObject);
                                });
                            } else
                                return filesToBeExcluded.allMatch(ifsObject::notEquals);
                        })

                        .collect(Collectors.toCollection(ArrayHList::new));
            }
        } else try (Stream<Path> pathStream = Files.list(directory.toPath())) {
            return pathStream
                    .filter(path -> {
                        return !asPath().equals(path) && filesToBeExcluded.noneMatch(obj -> path.equals(obj.asPath()));
                    })
                    .map(path -> {
                        return Files.isDirectory(path) ? new HDirectory(path) : new HFile(path);
                    })
                    .collect(Collectors.toCollection(ArrayHList::new));
        }
    }

    @Override
    public boolean isEmpty() throws IOException {
        checkExists(this);
        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory.toPath())) {
            return !files.iterator().hasNext();
        }
    }

    @Override
    public boolean isNotEmpty() throws IOException { return !isEmpty(); }

    @Override
    @NotRecursive
    public boolean hasOnlyFiles() throws IOException {
        checkExists(this);
        boolean hasFile = false;
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory.toPath())) {
            for (Path path : paths) {
                if (isNotDirectory(path)) {
                    hasFile = true;
                } else
                    return false;
            }

            return hasFile;
        }
    }

    @Override
    @NotRecursive
    public boolean hasOnlyDirectories() throws IOException {
        checkExists(this);
        boolean hasDirectory = false;
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory.toPath())) {
            for (Path path : paths) {
                if (Files.isDirectory(path)) {
                    hasDirectory = true;
                } else
                    return false;
            }

            return hasDirectory;
        }
    }

    @Override
    @NotRecursive
    public boolean notFoundInternalDirectories() throws IOException {
        checkExists(this);
        try(Stream<Path> pathStream = Files.list(directory.toPath())) {
            return pathStream.parallel().noneMatch(Files::isDirectory);
        }
    }

    @Override
    @NotRecursive
    public boolean notFoundInternalFiles() throws IOException {
        checkExists(this);
        try(Stream<Path> pathStream = Files.list(directory.toPath())) {
            return pathStream.parallel().noneMatch(this::isNotDirectory);
        }
    }

    @Override
    @MaybeRecursive
    public boolean contains(IFSObject object, boolean recursive) throws IOException {
        checkExists(this);
        if (recursive) {
            try (Stream<Path> pathStream = Files.walk(directory.toPath())) {
                return pathStream
                        .parallel()
                        .anyMatch(path -> object.asPath().equals(path));
            }
        } else try (Stream<Path> pathStream = Files.list(directory.toPath())) {
            return pathStream
                    .parallel()
                    .anyMatch(path -> object.asPath().equals(path));
        }
    }

    @Override
    @Recursive
    public HList<IFSObject> find(Predicate<? super IFSObject> matcher) throws IOException {
        checkExists(this);
        try (Stream<Path> pathStream = Files.find(directory.toPath(), Integer.MAX_VALUE, ((path, basicFileAttributes) -> {
            // maybe get the generic class through reflection?
            if (Files.isDirectory(path)) {
                return !directory.toPath().equals(path) && matcher.test(new HDirectory(path));
            } else
                return matcher.test(new HFile(path));
        }))) {
            return pathStream
                    .map(path -> {
                        return Files.isDirectory(path) ? new HDirectory(path) : new HFile(path);
                    })
                    .collect(Collectors.toCollection(ArrayHList::new));
        }
    }

    @Override
    @Recursive
    public HList<IFSObject> find(String glob) throws IOException {
        checkExists(this);
        if (glob.equals("*"))
            return getAllFiles(true);
        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
        try (Stream<Path> pathStream = Files.find(directory.toPath(), Integer.MAX_VALUE, ((path, basicFileAttributes) -> {
            return !path.equals(asPath()) && matcher.matches(path);
        }))) {
            return pathStream
                    .map(path -> {
                        return Files.isDirectory(path) ? new HDirectory(path) : new HFile(path);
                    })
                    .collect(Collectors.toCollection(ArrayHList::new));
        }
    }

    @Override
    @Recursive
    public HList<IFSObject> findByNames(String... names) throws IOException {
        checkExists(this);
        final HList<String> includeNames = HList.of(names);
        try (Stream<Path> pathStream = Files.walk(directory.toPath())) {
            return pathStream
                    .parallel()
                    .filter(path -> !directory.toPath().equals(path) && includeNames.anyMatch(name -> {
                        return FilenameUtils.equalsOnSystem(FilenameUtils.getName(path.toString()), name);
                    }))
                    .map(path -> {
                        return Files.isDirectory(path) ? new HDirectory(path) : new HFile(path);
                    })
                    .collect(Collectors.toCollection(ArrayHList::new));
        }
    }

    @Override
    @Recursive
    public HList<BaseDirectory> findDirectories(Predicate<? super BaseDirectory> matcher) throws IOException {
        checkExists(this);
        try (Stream<Path> pathStream = Files.find(directory.toPath(), Integer.MAX_VALUE, ((path, basicFileAttributes) -> {
            return Files.isDirectory(path) && !asPath().equals(path) && matcher.test(new HDirectory(path));
        }))) {
            return pathStream
                    .map(HDirectory::new)
                    .collect(Collectors.toCollection(ArrayHList::new));
        }
    }

    @Override
    @Recursive
    public HList<BaseDirectory> findDirectories(String glob) throws IOException {
        checkExists(this);
        if (glob.equals("*"))
            return getDirectories(true);
        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
        try (Stream<Path> pathStream = Files.find(directory.toPath(), Integer.MAX_VALUE, ((path, basicFileAttributes) -> {
            return Files.isDirectory(path) && !asPath().equals(path) && matcher.matches(path);
        }))) {
            return pathStream
                    .map(HDirectory::new)
                    .collect(Collectors.toCollection(ArrayHList::new));
        }
    }

    @Override
    @Recursive
    public HList<BaseDirectory> findDirectoriesByNames(String... names) throws IOException {
        // TODO MAYBE using glob?
        checkExists(this);
        final HList<String> includeNames = HList.of(names);
        try (Stream<Path> pathStream = Files.find(directory.toPath(), Integer.MAX_VALUE, ((path, basicFileAttributes) -> {
            return Files.isDirectory(path) && !asPath().equals(path) && includeNames.anyMatch(name -> {
                return FilenameUtils.equalsOnSystem(FilenameUtils.getName(path.toString()), name);
            });
        }))) {
            return pathStream
                    .map(HDirectory::new)
                    .collect(Collectors.toCollection(ArrayHList::new));
        }
    }

    @Override
    @Recursive
    public HList<BaseFile> findFiles(Predicate<? super BaseFile> matcher) throws IOException {
        checkExists(this);
        try (Stream<Path> pathStream = Files.find(directory.toPath(), Integer.MAX_VALUE, ((path, basicFileAttributes) -> {
            return isNotDirectory(path) && matcher.test(new HFile(path));
        }))) {
            return pathStream
                    .map(HFile::new)
                    .collect(Collectors.toCollection(ArrayHList::new));
        }
    }

    @Override
    @Recursive
    public HList<BaseFile> findFiles(String glob) throws IOException {
        checkExists(this);
        if (glob.equals("*"))
            return getFiles(true);
        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
        try (Stream<Path> pathStream = Files.find(directory.toPath(), Integer.MAX_VALUE, ((path, basicFileAttributes) -> {
            return isNotDirectory(path) && matcher.matches(path);
        }))) {
            return pathStream
                    .map(HFile::new)
                    .collect(Collectors.toCollection(ArrayHList::new));
        }
    }

    @Override
    @Recursive
    public HList<BaseFile> findFilesByNames(String... names) throws IOException {
        checkExists(this);
        final HList<String> includeNames = HList.of(names);
        try (Stream<Path> pathStream = Files.find(directory.toPath(), Integer.MAX_VALUE, ((path, basicFileAttributes) -> {
            return isNotDirectory(path) && includeNames.anyMatch(name -> {
                return FilenameUtils.equalsOnSystem(FilenameUtils.getName(path.toString()), name);
            });
        }))) {
            return pathStream
                    .map(HFile::new)
                    .collect(Collectors.toCollection(ArrayHList::new));
        }
    }

    @Override
    public boolean exists() { return this.directory.exists(); }
    @Override
    public boolean notExists() { return !this.directory.exists(); }
    @Override
    public HDirectory getParent() throws ParentNotFoundException {
        if (directory.toPath().getParent() != null) {
            return new HDirectory(directory.toPath().getParent());
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
        final HList<BaseDirectory> excludedDirectories = HList.of(directories);
        try (Stream<Path> pathStream = Files.walk(directory.toPath())) {
            HList<IFSObject> objects = pathStream
                    .parallel()
                    .filter(path -> {
                        if (Files.isDirectory(path)) {
                            return !directory.toPath().equals(path) && excludedDirectories.allMatch(excludedDir -> {
                                return excludedDir.notIsHierarchicalChild(new HDirectory(path)) && new HDirectory(path).notIsHierarchicalChild(excludedDir);
                            });
                        } else return excludedDirectories.allMatch(excludedDir -> new HFile(path).notIsHierarchicalChild(excludedDir));
                    })
                    .map(path -> {
                        return Files.isDirectory(path) ? new HDirectory(path) : new HFile(path);
                    })
                    .collect(Collectors.toCollection(ArrayHList::new));
            return objects.allMatch(obj -> {
                obj.delete();
                return obj.notExists();
            }, IOException.class);
        }
    }

    @Override
    @Recursive
    public <T extends BaseFile> boolean deleteExcept(T... files) throws IOException {
        checkExists(this);
        final HList<BaseFile> excludedFiles = HList.of(files);
        try (Stream<Path> pathStream = Files.walk(directory.toPath())) {
            HList<IFSObject> objects = pathStream
                    .parallel()
                    .filter(path -> {
                        if (Files.isDirectory(path)) {
                            return !directory.toPath().equals(path) && excludedFiles.allMatch(excludedDir -> {
                                return excludedDir.notIsHierarchicalChild(new HDirectory(path));
                            });
                        } else return excludedFiles.allMatch(file -> new HFile(path).notEquals(file));
                    })
                    .map(path -> {
                        return Files.isDirectory(path) ? new HDirectory(path) : new HFile(path);
                    })
                    .collect(Collectors.toCollection(ArrayHList::new));
            return objects.allMatch(obj -> {
                obj.delete();
                return obj.notExists();
            }, IOException.class);
        }
    }

    @Override
    @Recursive
    public boolean deleteExcept(HList<IFSObject> excludedFiles) throws IOException {
        checkExists(this);
        try (Stream<Path> pathStream = Files.walk(directory.toPath())) {
            HList<IFSObject> objects = pathStream
                    .parallel()
                    .filter(path -> {
                        if (Files.isDirectory(path)) {
                            return !directory.toPath().equals(path) && excludedFiles.allMatch(ifsObject -> {
                                if (ifsObject instanceof BaseDirectory) {
                                    return ifsObject.notIsHierarchicalChild(new HDirectory(path)) && new HDirectory(path).notIsHierarchicalChild((HDirectory) ifsObject);
                                } else
                                    return ifsObject.notIsHierarchicalChild(new HDirectory(path));
                            });
                        } else
                            return excludedFiles.allMatch(file -> {
                                return !isNotDirectory(file.asPath()) || new HFile(path).notEquals(file);
                            });
                    })
                    .map(path -> {
                        return Files.isDirectory(path) ? new HDirectory(path) : new HFile(path);
                    })
                    .collect(Collectors.toCollection(ArrayHList::new));
            return objects.allMatch(obj -> {
                obj.delete();
                return obj.notExists();
            }, IOException.class);
        }
    }

    @Override
    public boolean delete(String name) throws IOException {
        checkExists(this);
        File resource = new File(directory, name);
        if (!resource.exists()) throw new NoSuchFileException("File " + resource + " not found");
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
        moveContentToDirectory(directory, false);
    }

    @Override
    @NotRecursive
    public boolean checkCountFiles(Long count) throws IOException {
        checkExists(this);
        try (Stream<Path> pathStream = Files.list(directory.toPath())) {
            return pathStream.count() == count;
        }
    }

    @Override
    @NotRecursive
    public boolean checkCountOnlyFiles(Long count) throws IOException {
        checkExists(this);
        try (Stream<Path> pathStream = Files.list(directory.toPath())) {
            return pathStream
                    .parallel()
                    .filter(this::isNotDirectory)
                    .count() == count;
        }
    }

    @Override
    @NotRecursive
    public boolean checkCountOnlyDirectories(Long count) throws IOException {
        checkExists(this);
        try (Stream<Path> pathStream = Files.list(directory.toPath())) {
            return pathStream
                    .parallel()
                    .filter(Files::isDirectory)
                    .count() == count;
        }
    }

    @Override
    @NotRecursive
    public boolean hasOnly1File() throws IOException {
        checkExists(this);
        try (Stream<Path> pathStream = Files.list(directory.toPath())) {
            return pathStream
                    .filter(this::isNotDirectory)
                    .count() == 1L;
        }
    }

    @Override
    @NotRecursive
    public boolean hasOnly1File(String fileName) throws IOException {
        checkExists(this);
        try (Stream<Path> pathStream = Files.list(directory.toPath())) {
            List<Path> file = pathStream
                    .filter(this::isNotDirectory)
                    .collect(Collectors.toList());
            return file.size() == 1 && FilenameUtils.equalsOnSystem(FilenameUtils.getName(file.get(0).toString()), fileName);
        }
    }

    @Override
    @NotRecursive
    public boolean hasOnly1Directory() throws IOException {
        checkExists(this);
        try (Stream<Path> pathStream = Files.list(directory.toPath())) {
            return pathStream
                    .filter(Files::isDirectory)
                    .count() == 1L;
        }
    }

    @Override
    @NotRecursive
    public boolean hasOnly1Directory(String directoryName) throws IOException {
        checkExists(this);
        try (Stream<Path> pathStream = Files.list(directory.toPath())) {
            List<Path> file = pathStream
                    .filter(Files::isDirectory)
                    .collect(Collectors.toList());
            return file.size() == 1 && FilenameUtils.equalsOnSystem(FilenameUtils.getName(file.get(0).toString()), directoryName);
        }
    }

    @Override
    @NotRecursive
    public boolean hasOnly1FileOrDirectory() throws IOException {
        try (Stream<Path> pathStream = Files.list(directory.toPath())) {
            return pathStream.count() == 1L;
        }
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

    private boolean isHierarchicalChild0(BaseDirectory superParent) {
        if (this.directory.equals(superParent.directory)) return true;
        Path supDirectory = directory.toPath();
        while (supDirectory != null) {
            if (supDirectory.equals(superParent.directory.toPath())) {
                return true;
            } else supDirectory = supDirectory.getParent();
        }

        return false;
    }
}
