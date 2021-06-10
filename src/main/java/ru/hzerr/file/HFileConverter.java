package ru.hzerr.file;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

@Deprecated
public class HFileConverter {

    private HFileConverter() {}

    public static File toFile(HFile file) { return file.file; }
    public static Path toPath(HFile file) { return file.file.toPath(); }
    public static URL toURL(HFile file) throws MalformedURLException { return file.file.toURI().toURL(); }
    public static URI toURI(HFile file) { return file.file.toURI(); }

    public static File toFile(HDirectory directory) { return directory.directory; }
    public static Path toPath(HDirectory directory) { return directory.directory.toPath(); }
    public static URL toURL(HDirectory directory) throws MalformedURLException { return directory.directory.toURI().toURL(); }
    public static URI toURI(HDirectory directory) { return directory.directory.toURI(); }
}
