package ru.hzerr.file.exception.file;

public class HFileCreateImpossibleException extends HFileException {

    public HFileCreateImpossibleException() { super(); }
    public HFileCreateImpossibleException(String message) { super(message); }
    public HFileCreateImpossibleException(Exception e, String message) { super(e, message); }
}
