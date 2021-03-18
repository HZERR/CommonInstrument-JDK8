package ru.hzerr.file.exception.directory;

public class HDirectoryIsNotDirectoryException extends HDirectoryException {

    public HDirectoryIsNotDirectoryException() { super(); }
    public HDirectoryIsNotDirectoryException(String message) { super(message); }
    public HDirectoryIsNotDirectoryException(Exception e, String message) { super(e, message); }
}
