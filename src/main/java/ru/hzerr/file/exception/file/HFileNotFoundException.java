package ru.hzerr.file.exception.file;

public class HFileNotFoundException extends HFileException {

    public HFileNotFoundException() { super(); }
    public HFileNotFoundException(String message) { super(message); }
    public HFileNotFoundException(Exception e, String message) { super(e, message); }
}
