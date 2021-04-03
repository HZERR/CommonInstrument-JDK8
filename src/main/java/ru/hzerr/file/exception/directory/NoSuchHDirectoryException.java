package ru.hzerr.file.exception.directory;

public class NoSuchHDirectoryException extends RuntimeException {

    public NoSuchHDirectoryException(String file) {
        super(file);
    }
}
