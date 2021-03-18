package ru.hzerr.file.exception.file;

public class HFileCreationFailedException extends HFileException {

    public HFileCreationFailedException() { super(); }
    public HFileCreationFailedException(String message) { super(message); }
    public HFileCreationFailedException(Exception e, String message) { super(e, message); }
}
