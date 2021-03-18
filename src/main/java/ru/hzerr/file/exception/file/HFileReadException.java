package ru.hzerr.file.exception.file;

public class HFileReadException extends HFileException {

    public HFileReadException() { super(); }
    public HFileReadException(String message) { super(message); }
    public HFileReadException(Exception e, String message) { super(e, message); }
}
