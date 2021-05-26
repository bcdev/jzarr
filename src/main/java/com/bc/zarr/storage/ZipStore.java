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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ZipStore implements Store {
    private final FileSystem zfs;
    private final Path internalRoot;

    public ZipStore(Path zipFilePath) throws IOException {
        final HashMap<String, String> zipParams = new HashMap<>();
        if (!Files.exists(zipFilePath)) {
            zipParams.put("create", "true");
        }
        final URI uri = URI.create("jar:file:" + zipFilePath.toUri().getPath());
        zfs = FileSystems.newFileSystem(uri, zipParams);
        internalRoot = zfs.getRootDirectories().iterator().next();
    }

    @Override
    public InputStream getInputStream(String key) throws IOException {
        final Path path = internalRoot.resolve(key);
        if (Files.isReadable(path)) {
            byte[] bytes = Files.readAllBytes(path);
            return new ByteArrayInputStream(bytes);
        }
        return null;
    }

    @Override
    public OutputStream getOutputStream(String key) {
        return new ByteArrayOutputStream() {
            private boolean closed = false;

            @Override
            public void close() throws IOException {
                try {
                    if (!closed) {
                        final byte[] bytes = this.toByteArray();
                        final Path filePath = internalRoot.resolve(key);
                        if (Files.exists(filePath)) {
                            Files.delete(filePath);
                        } else {
                            Files.createDirectories(filePath.getParent());
                        }
                        Files.write(filePath, bytes);
                    }
                } finally {
                    closed = true;
                }
            }
        };
    }

    @Override
    public void delete(String key) throws IOException {
        final Path toBeDeleted = internalRoot.resolve(key);
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
        return Files.walk(internalRoot)
                .filter(path1 -> path1.toString().endsWith(suffix))
                .map(path -> internalRoot.relativize(path.getParent()).toString())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public void close() throws IOException {
        zfs.close();
    }
}
