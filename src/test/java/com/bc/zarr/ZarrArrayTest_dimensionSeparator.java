/*
 *
 * MIT License
 *
 * Copyright (c) 2021. Brockmann Consult GmbH (info@brockmann-consult.de)
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

package com.bc.zarr;

import com.bc.zarr.storage.FileSystemStore;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

public class ZarrArrayTest_dimensionSeparator {

    private Path arrayPath;
    private Path rootPath;

    @Before
    public void setUp() throws Exception {
        rootPath = Jimfs.newFileSystem().getPath("non", "existing", "path");
//        rootPath = Files.createTempDirectory("jzarr");
        arrayPath = rootPath.resolve("test").resolve("dir");
        final FileSystemStore store = new FileSystemStore(arrayPath);
        final ZarrArray array = ZarrArray.create(store, new ArrayParams()
                .shape(100, 100, 100)
                .chunks(10, 10, 10)
                .dataType(DataType.i1)
                .compressor(null)
                .dimensionSeparator(DimensionSeparator.SLASH));
        array.write(12, new int[]{10, 10, 10}, new int[]{5, 5, 5});
        // Preparation to remove the dimension_separator line from .zarray file.
        // This is needed to simulate an old stile header file without separator char.
        // In such a case it must be detected, if "." or "/" must be used as dimension separator char.
        prepareOldStyleArrayHeaderFile(store);
    }

    @After
    public void tearDown() throws Exception {
        final FileSystem fileSystem = rootPath.getFileSystem();
        final String className = fileSystem.getClass().getName();
        if (className.toLowerCase().contains("jimfs")) {
            return;
        }
        final FileSystem defaultFS = FileSystems.getDefault();
        if (!defaultFS.equals(fileSystem)) {
            return;
        }
        final List<Path> paths = Files.walk(rootPath).sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        for (Path path : paths) {
            Files.deleteIfExists(path);
        }
    }

    @Test
    public void openOldStyleZarrArrayAndDetectDimensionSeparator() throws IOException {
        final ZarrArray array = ZarrArray.open(arrayPath);
        DimensionSeparator sep = array.getDimensionSeparator();
        assertThat(sep).isNotNull();
        assertThat(sep.getSeparatorChar()).isEqualTo("/");
    }

    private void prepareOldStyleArrayHeaderFile(FileSystemStore store) throws IOException {
        final InputStream is = store.getInputStream(".zarray");
        final OutputStream os = store.getOutputStream(".zarray");
        TestUtils.deleteLineContaining("dimension_separator", is, os);
    }
}
