package ru.hzerr.file.exception;

import java.io.FileNotFoundException;

public class ParentNotFoundException extends FileNotFoundException {

    public ParentNotFoundException() { super(); }
    public ParentNotFoundException(String message) { super(message); }
}
