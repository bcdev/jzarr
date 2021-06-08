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

package com.bc.zarr.storage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class StoreTest_getRelativeLeafKeys {

    private Path storeRoot;

    @Before
    public void setUp() throws Exception {
        storeRoot = Files.createTempDirectory("JZarr");
    }

    @After
    public void tearDown() throws Exception {
        final List<Path> paths = Files.walk(storeRoot)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        for (Path path : paths) {
            Files.delete(path);
        }
    }

    @Test
    public void getRelativeLeafKeys_fromFileSystemStore() throws IOException {
        final Store store = new FileSystemStore(storeRoot);
        prepareStore(store);

        Stream<String> relativeLeafKeys;
        relativeLeafKeys = store.getRelativeLeafKeys("group/array");
        assertThat(relativeLeafKeys).containsExactlyInAnyOrderElementsOf(
                Arrays.asList(".zarray", "3/23/4", "4/23/4", "5/23/4")
        );
        relativeLeafKeys = store.getRelativeLeafKeys("group");
        assertThat(relativeLeafKeys).containsExactlyInAnyOrderElementsOf(
                Arrays.asList("array/.zarray", "array/3/23/4", "array/4/23/4", "array/5/23/4")
        );
    }

    @Test
    public void getRelativeLeafKeys_fromInMemoryStore() throws IOException {
        final Store store = new InMemoryStore();
        prepareStore(store);

        Stream<String> relativeLeafKeys;
        relativeLeafKeys = store.getRelativeLeafKeys("group/array");
        assertThat(relativeLeafKeys).containsExactlyInAnyOrderElementsOf(
                Arrays.asList(".zarray", "3/23/4", "4/23/4", "5/23/4")
        );
        relativeLeafKeys = store.getRelativeLeafKeys("group");
        assertThat(relativeLeafKeys).containsExactlyInAnyOrderElementsOf(
                Arrays.asList("array/.zarray", "array/3/23/4", "array/4/23/4", "array/5/23/4")
        );
    }

    @Test
    public void getRelativeLeafKeys_fromZipStore() throws IOException {
        try (Store store = new ZipStore(storeRoot.resolve("zipFile"))) {
            prepareStore(store);

            Stream<String> relativeLeafKeys;
            relativeLeafKeys = store.getRelativeLeafKeys("group/array");
            assertThat(relativeLeafKeys).containsExactlyInAnyOrderElementsOf(
                    Arrays.asList(".zarray", "3/23/4", "4/23/4", "5/23/4")
            );
            relativeLeafKeys = store.getRelativeLeafKeys("group");
            assertThat(relativeLeafKeys).containsExactlyInAnyOrderElementsOf(
                    Arrays.asList("array/.zarray", "array/3/23/4", "array/4/23/4", "array/5/23/4")
            );
        }
    }

    private void prepareStore(Store store) throws IOException {
        final String[] fileKeys = {
                "group/array/.zarray",
                "group/array/3/23/4",
                "group/array/4/23/4",
                "group/array/5/23/4",
        };
        for (String fileKey : fileKeys) {
            try (OutputStream os = store.getOutputStream(fileKey)) {
                os.write(new byte[]{1, 2, 3, 4, 5});
            }
        }
    }
}