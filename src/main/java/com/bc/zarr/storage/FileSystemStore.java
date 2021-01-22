/*
 *
 * MIT License
 *
 * Copyright (c) 2020. Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.bc.zarr.storage;

import com.bc.zarr.ZarrConstants;
import com.bc.zarr.ZarrUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class FileSystemStore implements Store {

    private final Path root;
    private final InputStreamCreatorStrategy strategy;

    public FileSystemStore(String path, FileSystem fileSystem) {
        this(fileSystem == null? Paths.get(path):fileSystem.getPath(path));
    }

    public FileSystemStore(Path rootPath) {
        this(rootPath, null);
    }

    public FileSystemStore(Path rootPath, InputStreamCreatorStrategy strategy) {
        root = rootPath;
        if (strategy != null) {
            this.strategy = strategy;
        } else {
            this.strategy = Files::newInputStream;
        }
    }

    @Override
    public InputStream getInputStream(String key) throws IOException {
        final Path path = root.resolve(key);
        if (Files.isReadable(path)) {
            return strategy.create(path);
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
        if (Files.exists(toBeDeleted)) {
            Files.delete(toBeDeleted);
        }
        if (Files.exists(toBeDeleted) || Files.isDirectory(toBeDeleted)) {
            throw new IOException("Unable to initialize " + toBeDeleted.toAbsolutePath().toString());
        }
    }

    @Override
    public TreeSet<String> getArrayKeys() throws IOException {
        return getKeysFor(ZarrConstants.FILENAME_DOT_ZARRAY);
    }

    @Override
    public TreeSet<String> getGroupKeys() throws IOException {
        return getKeysFor(ZarrConstants.FILENAME_DOT_ZGROUP);
    }

    private TreeSet<String> getKeysFor(String suffix) throws IOException {
        return Files.walk(root)
                .filter(path -> path.getFileName().toString().endsWith(suffix))
                .map(path -> root.relativize(path.getParent()).toString())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public interface InputStreamCreatorStrategy {
        InputStream create(Path path) throws IOException;
    }
}
