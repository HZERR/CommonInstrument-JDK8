package ru.hzerr.file;

import java.io.IOException;

public interface IFSObject extends IBackwardCompatibility, IObject {

    String getName();
    String getLocation();
    void create() throws IOException;
    boolean exists();
    boolean notExists();
    boolean delete() throws IOException;
    void deleteOnExit();
    <T extends BaseDirectory> T getParent();
    <T extends BaseDirectory> boolean isHierarchicalChild(T superParent);
    <T extends BaseDirectory> boolean notIsHierarchicalChild(T superParent);
    double sizeOf(SizeType type);
}
