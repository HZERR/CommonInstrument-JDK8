package ru.hzerr.file.stream;

import java.util.function.Consumer;

public interface IFileWrapper {

    void wrap(IFSObjects whichOneToApply, Consumer<Exception> onError);
}
