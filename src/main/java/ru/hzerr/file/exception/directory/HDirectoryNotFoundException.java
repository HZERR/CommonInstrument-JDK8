package ru.hzerr.file.exception.directory;

public class HDirectoryNotFoundException extends HDirectoryException {

    public HDirectoryNotFoundException() { super(); }
    public HDirectoryNotFoundException(String message) { super(message); }
    public HDirectoryNotFoundException(Exception e, String message) { super(e, message); }
}
