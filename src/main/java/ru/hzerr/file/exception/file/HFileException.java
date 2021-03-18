package ru.hzerr.file.exception.file;

import java.io.IOException;

public class HFileException extends IOException {

    public HFileException() { super(); }
    public HFileException(String message) { super(message); }
    public HFileException(Exception e, String message) { super(message, e); }
}
