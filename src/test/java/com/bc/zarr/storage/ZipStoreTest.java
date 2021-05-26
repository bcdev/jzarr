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

import com.bc.zarr.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.ByteOrder;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.bc.zarr.TestUtils.getPrivateFieldObject;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test along https://zarr.readthedocs.io/en/stable/api/storage.html#zarr.storage.DirectoryStore
 */
public class ZipStoreTest {

    private ZipStore store;
    private Path rootPath;
    private Path testDataPath;

    @Before
    public void setUp() throws Exception {
        testDataPath = Files.createTempDirectory("zarrTest");

        rootPath = testDataPath.resolve("zipped.znap.zip");

        store = new ZipStore(rootPath);
    }

    @After
    public void tearDown() throws Exception {
        final List<Path> paths = Files.walk(testDataPath).sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        for (Path path : paths) {
            Files.delete(path);
        }
    }

    @Test
    public void createFileSystemStore_withPathString_withoutFS() throws NoSuchFieldException, IllegalAccessException {
        //execution
        final FileSystem fileSystem = null;
        final String pathStr = "abc/def/ghi";
        final FileSystemStore fileSystemStore = new FileSystemStore(pathStr, fileSystem);

        //verification
        final Object root = getPrivateFieldObject(fileSystemStore, "internalRoot");
        assertThat(root).isInstanceOf(Path.class);
        final String expected = Paths.get(pathStr).toAbsolutePath().toString();
        final Path rootNioPath = (Path) root;
        assertThat(rootNioPath.getFileSystem()).isEqualTo(FileSystems.getDefault());
        assertThat(rootNioPath.toAbsolutePath().toString()).isEqualTo(expected);
    }

    @Test
    public void deleteDirFromStore() throws IOException, InvalidRangeException {
        //preparation
        final int[] shape = {10, 10};
        final int[] chunks = {5, 5};
        final byte[] data = new byte[100];
        Arrays.fill(data, (byte) 42);

        final ArrayParams parameters = new ArrayParams()
                .dataType(DataType.i1).shape(shape).chunks(chunks)
                .fillValue(0).compressor(null);

        final ZarrGroup rootGrp = ZarrGroup.create(store, null);
        final ZarrArray fooArray = rootGrp.createArray("toBeDeleted", parameters, null);
        fooArray.write(data, shape, new int[]{0, 0});

        assertThat(Files.isRegularFile(rootPath)).isTrue();
        assertThat(store.getGroupKeys().size()).isEqualTo(1);
        assertThat(store.getGroupKeys()).contains("");
        assertThat(store.getArrayKeys().size()).isEqualTo(1);
        assertThat(store.getArrayKeys()).contains("toBeDeleted");

        final String[] chunkFileNames = {"0.0", "0.1", "1.0", "1.1"};
        for (String name : chunkFileNames) {
            assertThat(store.getInputStream("/toBeDeleted/" + name)).isNotNull();
        }

        //execution
        store.delete("/toBeDeleted");

        //verification
        assertThat(Files.isRegularFile(rootPath)).isTrue();
        assertThat(store.getGroupKeys().size()).isEqualTo(1);
        assertThat(store.getGroupKeys()).contains("");
        assertThat(store.getArrayKeys().size()).isEqualTo(0);

        for (String name : chunkFileNames) {
            assertThat(store.getInputStream("/toBeDeleted/" + name)).isNull();
        }
    }

    @Test
    public void deleteSingleFileFromStore() throws IOException, InvalidRangeException {
        //preparation
        final int[] shape = {10, 10};
        final int[] chunks = {5, 5};
        final byte[] data = new byte[100];
        Arrays.fill(data, (byte) 42);

        final ArrayParams parameters = new ArrayParams()
                .dataType(DataType.i1).shape(shape).chunks(chunks)
                .fillValue(0).compressor(null);

        final ZarrGroup rootGrp = ZarrGroup.create(store, null);
        final ZarrArray fooArray = rootGrp.createArray("foo", parameters, null);
        fooArray.write(data, shape, new int[]{0, 0});

        assertThat(Files.isRegularFile(rootPath)).isTrue();
        assertThat(store.getGroupKeys().size()).isEqualTo(1);
        assertThat(store.getGroupKeys()).contains("");
        assertThat(store.getArrayKeys().size()).isEqualTo(1);
        assertThat(store.getArrayKeys()).contains("foo");

        final String[] chunkFileNames = {"0.0", "0.1", "1.0", "1.1"};
        for (String name : chunkFileNames) {
            assertThat(store.getInputStream("/foo/" + name)).isNotNull();
        }

        //execution
        store.delete("/foo/1.0");

        //verification
        assertThat(Files.isRegularFile(rootPath)).isTrue();
        assertThat(store.getGroupKeys().size()).isEqualTo(1);
        assertThat(store.getGroupKeys()).contains("");
        assertThat(store.getArrayKeys().size()).isEqualTo(1);
        assertThat(store.getArrayKeys()).contains("foo");

        final boolean[] expectations = {false, false, true, false};
        for (int i = 0; i < chunkFileNames.length; i++) {
            final InputStream is = store.getInputStream("/foo/" + chunkFileNames[i]);
            assertThat(is == null).isEqualTo(expectations[i]);
        }
    }

    @Test
    public void createGroup() throws IOException {
        //preparation
        final Map<String, Object> attributes = TestUtils.createMap("lsmf", 345, "menno", 23.23);

        //execution
        final ZarrGroup rootGrp = ZarrGroup.create(store, attributes);

        //verification
        assertThat(Files.isRegularFile(rootPath)).isTrue();
        assertThat(store.getArrayKeys().size()).isEqualTo(0);
        assertThat(store.getGroupKeys().size()).isEqualTo(1);

        final InputStream zgroupIS = store.getInputStream("/.zgroup");
        assertThat(zgroupIS).isNotNull();
        final String grpStr = readAll(zgroupIS);
        assertThat(grpStr).isNotNull();
        assertThat(strip(grpStr)).isEqualTo("{\"zarr_format\":2}");

        final InputStream zattrsIS = store.getInputStream("/.zattrs");
        assertThat(zattrsIS).isNotNull();
        final String attStr = readAll(zattrsIS);
        assertThat(attStr).isNotNull();
        assertThat(strip(attStr)).isEqualTo("{\"lsmf\":345,\"menno\":23.23}");
    }

    @Test
    public void createArray() throws IOException, JZarrException {
        //preparation
        final int[] shape = {10, 10};
        final int[] chunks = {5, 5};
        final byte[] data = new byte[100];
        Arrays.fill(data, (byte) 42);
        final Map<String, Object> attributes = TestUtils.createMap("data", new double[]{4, 5, 6, 7, 8});

        //execution
        final ZarrGroup rootGrp = ZarrGroup.create(store, null);
        final ArrayParams parameters = new ArrayParams()
                .dataType(DataType.i1)
                .shape(shape)
                .chunks(chunks)
                .byteOrder(ByteOrder.LITTLE_ENDIAN)
                .fillValue(0)
                .compressor(null);
        final ZarrArray fooArray = rootGrp.createArray("foo", parameters, attributes);

        //verification
        assertThat(Files.isRegularFile(rootPath)).isTrue();
        assertThat(store.getGroupKeys().size()).isEqualTo(1);
        assertThat(store.getGroupKeys()).contains("");
        assertThat(store.getArrayKeys().size()).isEqualTo(1);
        assertThat(store.getArrayKeys()).contains("foo");

        final ZarrHeader header = new ZarrHeader(shape, chunks, DataType.i1.toString(), ByteOrder.LITTLE_ENDIAN, 0, null);
        final String expected = strip(ZarrUtils.toJson(header, true));
        final InputStream zarrayIS = store.getInputStream("/foo/.zarray");
        assertThat(zarrayIS).isNotNull();
        assertThat(strip(readAll(zarrayIS))).isEqualToIgnoringWhitespace(expected);

        final InputStream zattrsIS = store.getInputStream("/foo/.zattrs");
        assertThat(zattrsIS).isNotNull();
        assertThat(strip(readAll(zattrsIS))).isEqualTo("{\"data\":[4.0,5.0,6.0,7.0,8.0]}");
    }

    @Test
    public void writeArrayDataChunked() throws IOException, InvalidRangeException {
        //preparation
        final int[] shape = {10, 10};
        final int[] chunks = {5, 5};
        final byte[] data = new byte[100];
        Arrays.fill(data, (byte) 42);
        final Map<String, Object> attributes = TestUtils.createMap("data", new double[]{4, 5, 6, 7, 8});

        final ArrayParams parameters = new ArrayParams()
                .dataType(DataType.i1).shape(shape).chunks(chunks)
                .fillValue(0).compressor(null);

        //execution
        final ZarrGroup rootGrp = ZarrGroup.create(store, null);
        final ZarrArray fooArray = rootGrp.createArray("foo", parameters, attributes);
        fooArray.write(data, shape, new int[]{0, 0});

        //verification
        assertThat(Files.isRegularFile(rootPath)).isTrue();
        assertThat(store.getGroupKeys().size()).isEqualTo(1);
        assertThat(store.getGroupKeys()).contains("");
        assertThat(store.getArrayKeys().size()).isEqualTo(1);
        assertThat(store.getArrayKeys()).contains("foo");

        final String[] chunkFileNames = {"0.0", "0.1", "1.0", "1.1"};
        final byte[] expectedBytes = new byte[25];
        Arrays.fill(expectedBytes, (byte) 42);
        for (String name : chunkFileNames) {
            final InputStream is = store.getInputStream("/foo/" + name);
            assertThat(is).isNotNull();
            final byte[] biggerBuffer = new byte[200];
            assertThat(is.read(biggerBuffer)).isEqualTo(25);
            assertThat(Arrays.copyOf(biggerBuffer, 25)).isEqualTo(expectedBytes);
        }
    }

    @Test
    public void createSubGroup() throws IOException {
        //preparation
        final Map<String, Object> attributes = TestUtils.createMap("aaaa", "pfrt", "y", 123);

        //execution
        final ZarrGroup rootGrp = ZarrGroup.create(store, null);
        final ZarrGroup foo = rootGrp.createSubGroup("foo", attributes);

        //verification
        assertThat(Files.isRegularFile(rootPath)).isTrue();
        assertThat(store.getGroupKeys().size()).isEqualTo(2);
        assertThat(store.getGroupKeys()).containsAll(Arrays.asList("", "foo"));

        assertThat(store.getArrayKeys().size()).isEqualTo(0);

        final InputStream zgroupIS = store.getInputStream("/foo/.zgroup");
        assertThat(zgroupIS).isNotNull();
        final String grpStr = readAll(zgroupIS);
        assertThat(grpStr).isNotNull();
        assertThat(strip(grpStr)).isEqualTo("{\"zarr_format\":2}");

        final InputStream zattrsIS = store.getInputStream("/foo/.zattrs");
        assertThat(zattrsIS).isNotNull();
        final String attStr = readAll(zattrsIS);
        assertThat(attStr).isNotNull();
        assertThat(strip(attStr)).isEqualTo("{\"y\":123,\"aaaa\":\"pfrt\"}");
    }

    private String readAll(InputStream attrsStream) {
        return new LineNumberReader(new InputStreamReader(attrsStream))
                .lines().collect(Collectors.joining());
    }

    private String strip(String s) {
        s = s.replace("\r", "").replace("\n", "");
        s = s.replace(" ", "");
//        while (s.contains("  ")) s = s.replace("  ", " ");
        return s;
    }


}