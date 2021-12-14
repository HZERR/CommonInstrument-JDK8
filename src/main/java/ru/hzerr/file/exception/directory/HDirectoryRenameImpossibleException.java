package ru.hzerr.file.exception.directory;

public class HDirectoryRenameImpossibleException extends HDirectoryException {

    public HDirectoryRenameImpossibleException() { super(); }
    public HDirectoryRenameImpossibleException(String message) { super(message); }
    public HDirectoryRenameImpossibleException(Exception e, String message) { super(e, message); }
}
