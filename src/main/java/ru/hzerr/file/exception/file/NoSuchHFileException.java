package ru.hzerr.file.exception.file;

public class NoSuchHFileException extends RuntimeException {

    public NoSuchHFileException(String file) {
        super(file);
    }
}
