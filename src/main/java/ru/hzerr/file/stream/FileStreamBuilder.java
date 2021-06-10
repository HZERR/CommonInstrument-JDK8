package ru.hzerr.file.stream;

import ru.hzerr.collections.list.ArrayHList;
import ru.hzerr.collections.list.HList;
import ru.hzerr.file.HDirectory;
import ru.hzerr.file.HFile;
import ru.hzerr.file.IFSObject;
import ru.hzerr.stream.HStream;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileStreamBuilder {

    private HList<HDirectory> directories = new ArrayHList<>();
    private HList<HFile> files = new ArrayHList<>();

    private FileStreamBuilder() {
    }

    public FileStreamBuilder add(HDirectory... directories) { this.directories.setAll(directories); return this; }
    public FileStreamBuilder add(HFile... files) { this.files.setAll(files); return this; }
    public FileStreamBuilder add(Path... paths) {
        for (Path path: paths) {
            if (Files.isDirectory(path)) {
                directories.add(new HDirectory(path.toString()));
            } else
                files.add(new HFile(path.toString()));
        }

        return this;
    }
    public FileStreamBuilder add(IFSObject... objects) {
        for (IFSObject object: objects) {
            if (Files.isDirectory(object.asPath())) {
                directories.add((HDirectory) object);
            } else
                files.add((HFile) object);
        }

        return this;
    }
    public FileStreamBuilder add(HStream<IFSObject> objects) {
        objects.biForEach(
                o -> Files.isDirectory(o.asPath()),
                o -> directories.add((HDirectory) o),
                o -> files.add((HFile) o)
        );

        return this;
    }

    public FileStream newStream() {
        return new FileStream(HStream.of(directories), HStream.of(files));
    }

    public static FileStreamBuilder newBuilder() { return new FileStreamBuilder(); }
}
