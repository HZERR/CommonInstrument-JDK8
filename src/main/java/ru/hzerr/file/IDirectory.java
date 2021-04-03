package ru.hzerr.file;

import ru.hzerr.stream.HStream;
import ru.hzerr.stream.bi.DoubleHStream;

import java.io.IOException;

public interface IDirectory extends IFSObject {

    <T extends IDirectory> T createSubDirectory(String dirName) throws IOException;
    <T extends IFile> T createSubFile(String fileName) throws IOException;
    <T extends IDirectory> T getSubDirectory(String dirName);
    <T extends IFile> T getSubFile(String fileName);

    <T extends IFile> HStream<T> getFiles();
    <T extends IDirectory> HStream<T> getDirectories();
    <ID extends IDirectory, IF extends IFile> DoubleHStream<ID, IF> getFiles(boolean recursive) throws IOException;

    boolean clean();

    <T extends IDirectory> boolean deleteExcept(T... directories) throws IOException;
    <T extends IFile> boolean deleteExcept(T... files) throws IOException;

    <ID extends IDirectory, IF extends IFile>
    boolean deleteExcept(DoubleHStream<ID, IF> excludedFiles) throws IOException;
    boolean delete(String dirOrFileName) throws IOException;

    <T extends IDirectory>
    void copyToDirectory(T directory) throws IOException;
    <T extends IDirectory>
    void copyContentToDirectory(T directory) throws IOException;
    <T extends IDirectory>
    void moveToDirectory(T directory) throws IOException;
    <T extends IDirectory>
    void moveContentToDirectory(T directory) throws IOException;
}
