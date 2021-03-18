package ru.hzerr.file;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class FSObject {

    private final HFile file;
    private final HDirectory directory;

    public FSObject(File file) {
        if (file.isDirectory()) {
            this.directory = new HDirectory(file.getAbsolutePath());
            this.file = null;
        } else {
            this.directory = null;
            this.file = new HFile(file.getAbsolutePath());
        }
    }

    public FSObject(Path filepath) {
        if (Files.isDirectory(filepath)) {
            this.directory = new HDirectory(filepath.toFile().getAbsolutePath());
            this.file = null;
        } else {
            this.directory = null;
            this.file = new HFile(filepath.toFile().getAbsolutePath());
        }
    }

    public HFile getFile() {
        if (file == null) throw new UnsupportedOperationException("This file system object is a directory");
        return file;
    }

    public HDirectory getDirectory() {
        if (directory == null) throw new UnsupportedOperationException("This file system object is a file");
        return directory;
    }

    public boolean isFile() { return file != null; }
    public boolean isDirectory() { return directory != null; }
}
