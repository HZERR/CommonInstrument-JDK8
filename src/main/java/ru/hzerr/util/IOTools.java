package ru.hzerr.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public final class IOTools {

    public static void getResourceAsStream(String resource, Consumer<? super InputStream> action, Consumer<? super IOException> onError) {
        InputStream iStream = null;
        try {
            iStream = IOTools.class.getResourceAsStream(resource);
            if (iStream == null) throw new FileNotFoundException("The " + resource + " resource not found");
            action.accept(iStream);
            iStream.close();
        } catch (IOException io) {
            onError.accept(io);
        } finally {
            org.apache.commons.io.IOUtils.closeQuietly(iStream);
        }
    }

    public static void getResourceAsStream(String resource, Consumer<? super InputStream> action) throws IOException {
        InputStream iStream = null;
        IOException e = null;
        try {
            iStream = IOTools.class.getResourceAsStream(resource);
            if (iStream == null) throw new FileNotFoundException("The " + resource + " resource not found");
            action.accept(iStream);
            iStream.close();
        } catch (IOException io) {
            e = io;
        } finally {
            org.apache.commons.io.IOUtils.closeQuietly(iStream);
        }
        if (e != null) {
            throw e;
        }
    }

    public static <T> T getResourceAsStream(String resource, Function<? super InputStream, T> action, Consumer<? super IOException> onError) {
        InputStream iStream = null;
        T result = null;
        try {
            iStream = IOTools.class.getResourceAsStream(resource);
            if (iStream == null) throw new FileNotFoundException("The " + resource + " resource not found");
            result = action.apply(iStream);
            iStream.close();
        } catch (IOException io) {
            onError.accept(io);
        } finally {
            org.apache.commons.io.IOUtils.closeQuietly(iStream);
        }

        return result;
    }

    public static <T> T getResourceAsStream(String resource, Function<? super InputStream, T> action) throws IOException {
        InputStream iStream = null;
        IOException e = null;
        T result = null;
        try {
            iStream = IOTools.class.getResourceAsStream(resource);
            if (iStream == null) throw new FileNotFoundException("The " + resource + " resource not found");
            result = action.apply(iStream);
            iStream.close();
        } catch (IOException io) {
            e = io;
        } finally {
            org.apache.commons.io.IOUtils.closeQuietly(iStream);
        }

        if (e != null) {
            throw e;
        }

        return result;
    }
}
