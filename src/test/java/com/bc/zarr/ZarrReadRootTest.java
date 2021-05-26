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

package com.bc.zarr;

import com.bc.zarr.storage.FileSystemStore;
import com.google.common.jimfs.Jimfs;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static com.bc.zarr.ZarrConstants.FILENAME_DOT_ZGROUP;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.*;

public class ZarrReadRootTest {

    private Path rootPath;

    @Before
    public void setUp() throws Exception {
        rootPath = Jimfs.newFileSystem().getPath("lsmf");
        Files.createDirectories(rootPath);
        final Path dotGroupPath = rootPath.resolve(FILENAME_DOT_ZGROUP);
        try (final Writer w = Files.newBufferedWriter(dotGroupPath)) {
            ZarrUtils.toJson(Collections.singletonMap("zarr_format", 2), w);
        }
    }

    @Test
    public void create() throws NoSuchFieldException, IllegalAccessException, IOException {
        final ZarrGroup rootGrp = ZarrGroup.open(rootPath);
        final Compressor compressor = CompressorFactory.create("zlib", "level", 1);
        final ArrayParams parameters = new ArrayParams()
                .dataType(DataType.f4)
                .shape(101, 102)
                .chunks(11, 12)
                .fillValue(4.2)
                .compressor(compressor);
        final ZarrArray arrayData = rootGrp.createArray("rastername", parameters, null);

        final String name = "relativePath";
        final Object path = TestUtils.getPrivateFieldObject(arrayData, name);
        assertThat(path, is(instanceOf(ZarrPath.class)));
        assertThat(((ZarrPath)path).storeKey, is("rastername"));
        final Object store = TestUtils.getPrivateFieldObject(rootGrp, "store");
        assertThat(store, is(instanceOf(FileSystemStore.class)));
        final Object root = TestUtils.getPrivateFieldObject(store, "internalRoot");
        assertThat(root, is(instanceOf(Path.class)));
        assertThat(root.toString(), is("lsmf"));
    }

}