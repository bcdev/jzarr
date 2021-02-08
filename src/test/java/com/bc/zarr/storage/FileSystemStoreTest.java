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

import com.bc.zarr.*;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.bc.zarr.TestUtils.*;
import static com.bc.zarr.ZarrConstants.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Test along https://zarr.readthedocs.io/en/stable/api/storage.html#zarr.storage.DirectoryStore
 */
@RunWith(Parameterized.class)
public class FileSystemStoreTest {

    private String testDataStr;
    private String rootPathStr;
    private FileSystem fs;
    private FileSystemStore store;
    private Path rootPath;
    private Path testDataPath;

    // Parameters
    private final Boolean nested;

    @Parameterized.Parameters
    public static Collection<Object[]> data(){
        return Arrays.asList(new Object[][] {
                {true},
                {false},
                {null}
        });
    }

    public FileSystemStoreTest(Boolean nested) {
        this.nested = nested;
    }

    @Before
    public void setUp() throws Exception {
        testDataStr = String.format("testData_%s", nested);
        rootPathStr = "group.zarr";

        final int fileSystemAlternative = 1;
        if (fileSystemAlternative == 1) {
            fs = Jimfs.newFileSystem(Configuration.unix());
            testDataPath = fs.getPath(testDataStr);
        }
        if (fileSystemAlternative == 2) {
            testDataPath = Files.createTempDirectory("zarrTest");
            fs = testDataPath.getFileSystem();
        }

        rootPath = testDataPath.resolve(rootPathStr);

        final int storeCreationAlternative = 1;
        if (storeCreationAlternative == 1) {
            store = new FileSystemStore(rootPath);
        }
        if (storeCreationAlternative == 2) {
            store = new FileSystemStore(rootPathStr, fs);
        }

        assertThat(Files.isDirectory(rootPath), is(false));
        assertThat(Files.exists(rootPath), is(false));
    }

    @After
    public void tearDown() throws Exception {
        if (!fs.getClass().getSimpleName().equals("JimfsFileSystem")) {
            final List<Path> paths = Files.walk(testDataPath).sorted(Comparator.reverseOrder()).collect(Collectors.toList());
            for (Path path : paths) {
                Files.delete(path);
            }
        }
    }

    @Test
    public void createFileSystemStore_withPathStringAndJimfsUnix() throws NoSuchFieldException, IllegalAccessException {
        //execution
        final FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix().toBuilder()
                .setWorkingDirectory("/some/working/dir").build());
        final FileSystemStore fileSystemStore = new FileSystemStore("abc/def/ghi", fileSystem);

        //verification
        final Object root = getPrivateFieldObject(fileSystemStore, "root");
        assertThat(root.getClass().getSimpleName(), is("JimfsPath"));
        assertThat(((Path) root).toAbsolutePath().toString(), is("/some/working/dir/abc/def/ghi"));
    }

    @Test
    public void createFileSystemStore_withPathString_withoutFS() throws NoSuchFieldException, IllegalAccessException {
        //execution
        final FileSystem fileSystem = null;
        final String pathStr = "abc/def/ghi";
        final FileSystemStore fileSystemStore = new FileSystemStore(pathStr, fileSystem);

        //verification
        final Object root = getPrivateFieldObject(fileSystemStore, "root");
        assertThat(root, is(instanceOf(Path.class)));
        final String expected = Paths.get(pathStr).toAbsolutePath().toString();
        final Path rootNioPath = (Path) root;
        assertThat(rootNioPath.getFileSystem(), is(equalTo(FileSystems.getDefault())));
        assertThat(rootNioPath.toAbsolutePath().toString(), is(expected));
    }

    @Test
    public void deleteDirFromStore() throws IOException {
        //preparation
        final Path toBeDeleted = rootPath.resolve("toBeDeleted");
        final Path someOtherDir = toBeDeleted.resolve("someOtherDir");
        Files.createDirectories(someOtherDir);
        final Path someFile = someOtherDir.resolve("someFile");
        Files.write(someFile, new byte[]{4, 3, 5, 6, 2});
        assertThat(Files.isRegularFile(someFile), is(true));
        assertThat(Files.isReadable(someFile), is(true));
        assertThat(Files.isDirectory(toBeDeleted), is(true));

        //execution
        store.delete("toBeDeleted");

        //verification
        assertThat(Files.isDirectory(toBeDeleted), is(false));

    }

    @Test
    public void deleteSingleFileFromStore() throws IOException {
        //preparation
        final Path someDirectory = rootPath.resolve("someDirectory");
        final Path toBeDeleted = someDirectory.resolve("toBeDeleted");
        Files.createDirectories(someDirectory);
        Files.write(toBeDeleted, new byte[]{4, 3, 5, 6, 2});
        assertThat(Files.isRegularFile(toBeDeleted), is(true));
        assertThat(Files.isReadable(toBeDeleted), is(true));

        //execution
        store.delete("someDirectory/toBeDeleted");

        //verification
        assertThat(Files.exists(toBeDeleted), is(false));

    }

    @Test
    public void createGroup() throws IOException {
        //preparation
        final Map<String, Object> attributes = TestUtils.createMap("lsmf", 345, "menno", 23.23);

        //execution
        final ZarrGroup rootGrp = ZarrGroup.create(store, attributes);

        //verification
        assertThat(Files.isDirectory(rootPath), is(true));
        assertThat(Files.list(rootPath).count(), is(2L));
        assertThat(Files.isReadable(rootPath.resolve(FILENAME_DOT_ZGROUP)), is(true));
        assertThat(Files.isReadable(rootPath.resolve(FILENAME_DOT_ZATTRS)), is(true));
        assertThat(strip(getZgroupContent(rootPath)), is("{\"zarr_format\":2}"));
        assertThat(strip(getZattrsContent(rootPath)), is("{\"lsmf\":345,\"menno\":23.23}"));
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
        final Path fooPath = rootPath.resolve("foo");
        assertThat(Files.isDirectory(fooPath), is(true));
        assertThat(Files.list(fooPath).count(), is(2L));
        assertThat(Files.isReadable(fooPath.resolve(FILENAME_DOT_ZARRAY)), is(true));
        assertThat(Files.isReadable(fooPath.resolve(FILENAME_DOT_ZATTRS)), is(true));
        final ZarrHeader header = new ZarrHeader(shape, chunks, DataType.i1.toString(), ByteOrder.LITTLE_ENDIAN, 0, null, null);
        final String expected = strip(ZarrUtils.toJson(header, true));
        assertThat(strip(getZarrayContent(fooPath)), is(equalToIgnoringWhiteSpace(expected)));
        assertThat(strip(getZattrsContent(fooPath)), is("{\"data\":[4.0,5.0,6.0,7.0,8.0]}"));
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
                .fillValue(0).compressor(null).nested(nested);

        //execution
        final ZarrGroup rootGrp = ZarrGroup.create(store, null);
        final ZarrArray fooArray = rootGrp.createArray("foo", parameters, attributes);
        fooArray.write(data, shape, new int[]{0, 0});

        //verification
        final Path fooPath = rootPath.resolve("foo");
        assertThat(Files.isDirectory(fooPath), is(true));

        String[] chunkFileNames;
        if (nested != null && nested) {
            List<Path> names = Files.list(fooPath).collect(Collectors.toList());
            assertThat(names.toString(),
                    Files.list(fooPath).filter(
                    path -> !path.getFileName().toString().startsWith(".")
            ).count(), is(2L));
            chunkFileNames = new String[]{"0/0", "0/1", "1/0", "1/1"};
        } else {
            assertThat(Files.list(fooPath).filter(
                    path -> !path.getFileName().toString().startsWith(".")
            ).count(), is(4L));
            chunkFileNames = new String[]{"0.0", "0.1", "1.0", "1.1"};
        }

        final byte[] expectedBytes = new byte[25];
        Arrays.fill(expectedBytes, (byte) 42);
        for (String name : chunkFileNames) {
            final Path chunkPath = fooPath.resolve(name);
            assertThat(Files.isReadable(chunkPath), is(true));
            assertThat(Files.size(chunkPath), is(5L * 5));
            assertThat(Files.readAllBytes(chunkPath), is(expectedBytes));
        }

        // Reopen the array to check that nesting v. flatness is detected.
        final ZarrGroup rootGrp2 = ZarrGroup.create(store, null);
        final ZarrArray fooArray2 = rootGrp.openArray("foo");
        if (nested == null) {
            // If any data is written, then the workaround check in ZarrArray.open
            // to find the first chunk (either *.* or */*) will take precedence.
            assertEquals(false, fooArray2.getNested());
        } else {
            assertEquals(nested, fooArray2.getNested());
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
        final Path fooPath = rootPath.resolve("foo");
        assertThat(Files.isDirectory(fooPath), is(true));
        assertThat(Files.list(fooPath).count(), is(2L));
        assertThat(Files.isReadable(fooPath.resolve(FILENAME_DOT_ZGROUP)), is(true));
        assertThat(Files.isReadable(fooPath.resolve(FILENAME_DOT_ZATTRS)), is(true));
        assertThat(strip(getZgroupContent(fooPath)), is("{\"zarr_format\":2}"));
        assertThat(strip(getZattrsContent(fooPath)), is("{\"y\":123,\"aaaa\":\"pfrt\"}"));
    }

    private String strip(String s) {
        s = s.replace("\r", "").replace("\n", "");
        s = s.replace(" ", "");
//        while (s.contains("  ")) s = s.replace("  ", " ");
        return s;
    }


}