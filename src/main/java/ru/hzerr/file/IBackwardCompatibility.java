package ru.hzerr.file;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

public interface IBackwardCompatibility {

    // Converts the current file to a path
    Path asPath();
    // Converts the current file into a java.io.File object
    File asIOFile();
    // Converts the current file to URI
    URI asURI();
    // Converts the current file to a URL
    URL asURL() throws MalformedURLException;
}
