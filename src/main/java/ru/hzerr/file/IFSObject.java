package ru.hzerr.file;

import ru.hzerr.file.exception.ParentNotFoundException;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;

public interface IFSObject extends IBackwardCompatibility, IObject, Serializable {

    String getName();
    String getLocation();
    void create() throws IOException;
    boolean exists();
    boolean notExists();
    boolean delete() throws IOException;
    boolean deleteIfPresent() throws IOException;
    void deleteOnExit();
    void rename(String fileName) throws IOException;
    <T extends BaseDirectory> T getParent() throws ParentNotFoundException;
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    <T extends BaseDirectory> boolean isHierarchicalChild(T superParent);
    <T extends BaseDirectory> boolean notIsHierarchicalChild(T superParent);
    double sizeOf(SizeType type);
    BigDecimal sizeOfAsBigDecimal(SizeType type);
}
