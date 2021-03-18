package ru.hzerr.file.exception.directory;

public class HDirectoryCreateImpossibleException extends HDirectoryException {

    public HDirectoryCreateImpossibleException() { super(); }
    public HDirectoryCreateImpossibleException(String message) { super(message); }
    public HDirectoryCreateImpossibleException(Exception e, String message) { super(e, message); }
}
