package ru.hzerr.file.exception.file;

public class HFileRenameFailedException extends HFileException {

    public HFileRenameFailedException() { super(); }
    public HFileRenameFailedException(String message) { super(message); }
    public HFileRenameFailedException(Exception e, String message) { super(e, message); }
}
