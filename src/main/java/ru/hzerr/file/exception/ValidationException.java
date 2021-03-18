package ru.hzerr.file.exception;

public class ValidationException extends RuntimeException {

    public ValidationException() { super(); }
    public ValidationException(String message) { super(message); }
    public ValidationException(Exception e, String message) { super(message, e); }
}
