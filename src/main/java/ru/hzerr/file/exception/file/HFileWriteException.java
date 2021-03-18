package ru.hzerr.file.exception.file;

public class HFileWriteException extends HFileException {

    public HFileWriteException() { super(); }
    public HFileWriteException(String message) { super(message); }
    public HFileWriteException(Exception e, String message) { super(e, message); }
}
