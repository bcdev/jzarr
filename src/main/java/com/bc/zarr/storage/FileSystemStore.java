package com.bc.zarr.storage;

import com.bc.zarr.ZarrUtils;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.commons.io.FileUtils.deleteDirectory;

public class FileSystemStore implements Store {

    private Path root;

    public FileSystemStore(String path, FileSystem fileSystem) {
        if (fileSystem == null) {
            root = Paths.get(path);
        } else {
            root = fileSystem.getPath(path);
        }
    }

    public FileSystemStore(Path rootPath) {
        root = rootPath;
    }

    @Override
    public InputStream getInputStream(String key) throws IOException {
        final Path path = root.resolve(key);
        if (Files.isReadable(path)) {
            return Files.newInputStream(path);
        } else {
            return null;
        }
    }

    @Override
    public OutputStream getOutputStream(String key) throws IOException {
        final Path filePath = root.resolve(key);
        final Path dir = filePath.getParent();
        Files.createDirectories(dir);
        return Files.newOutputStream(filePath);
    }

    @Override
    public void delete(String key) throws IOException {
        final Path toBeDeleted = root.resolve(key);
        if (Files.isDirectory(toBeDeleted)) {
            ZarrUtils.deleteDirectoryTreeRecursively(toBeDeleted);
        }
        if (Files.exists(toBeDeleted)){
            Files.delete(toBeDeleted);
        }
        if (Files.exists(toBeDeleted)|| Files.isDirectory(toBeDeleted)) {
            throw new IOException("Unable to initialize " + toBeDeleted.toAbsolutePath().toString());
        }
    }
}
