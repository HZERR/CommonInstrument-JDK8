package ru.hzerr.file.exception.directory;

public class HDirectoryRenameFailedException extends HDirectoryException {

    public HDirectoryRenameFailedException() { super(); }
    public HDirectoryRenameFailedException(String message) { super(message); }
    public HDirectoryRenameFailedException(Exception e, String message) { super(e, message); }
}
