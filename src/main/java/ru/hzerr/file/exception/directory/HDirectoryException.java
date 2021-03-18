package ru.hzerr.file.exception.directory;

import java.io.IOException;

public class HDirectoryException extends IOException {

    public HDirectoryException() { super(); }
    public HDirectoryException(String message) { super(message); }
    public HDirectoryException(Exception e, String message) { super(message, e); }
}
