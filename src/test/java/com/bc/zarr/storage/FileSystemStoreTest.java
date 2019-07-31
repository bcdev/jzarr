package com.bc.zarr.storage;

import com.bc.zarr.*;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
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
public class FileSystemStoreTest {

    private String testDataStr;
    private String rootPathStr;
    private FileSystem fs;
    private FileSystemStore store;
    private Path rootPath;
    private Path testDataPath;

    @Before
    public void setUp() throws Exception {
        testDataStr = "testData";
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
    public void createGroup() throws IOException, InvalidRangeException {
        //preparation
        final Map<String, Object> attributes = TestUtils.createMap("lsmf", 345, "menno", 23.23);

        //execution
        final ZarrGroup rootGrp = ZarrGroup.create(store, attributes);

        //verification
        assertThat(Files.isDirectory(rootPath), is(true));
        assertThat(Files.list(rootPath).count(), is(2L));
        assertThat(Files.isReadable(rootPath.resolve(FILENAME_DOT_ZGROUP)), is(true));
        assertThat(Files.isReadable(rootPath.resolve(FILENAME_DOT_ZATTRS)), is(true));
        assertThat(getZgroupContent(rootPath), is("{\"zarr_format\":2}"));
        assertThat(getZattrsContent(rootPath), is("{\"lsmf\":345,\"menno\":23.23}"));
    }

    @Test
    public void createArray() throws IOException {
        //preparation
        final int[] shape = {10, 10};
        final int[] chunks = {5, 5};
        final byte[] data = new byte[100];
        Arrays.fill(data, (byte) 42);
        final Map<String, Object> attributes = TestUtils.createMap("data", new double[]{4, 5, 6, 7, 8});

        //execution
        final ZarrGroup rootGrp = ZarrGroup.create(store, null);
        final ZarrArray fooArray = rootGrp.createArray("foo", ZarrDataType.i1, shape, chunks, 0, null, attributes);

        //verification
        final Path fooPath = rootPath.resolve("foo");
        assertThat(Files.isDirectory(fooPath), is(true));
        assertThat(Files.list(fooPath).count(), is(2L));
        assertThat(Files.isReadable(fooPath.resolve(FILENAME_DOT_ZARRAY)), is(true));
        assertThat(Files.isReadable(fooPath.resolve(FILENAME_DOT_ZATTRS)), is(true));
        final ZarrHeader header = new ZarrHeader(shape, chunks, ZarrDataType.i1.toString(), 0, null);
        final String expected = strip(ZarrUtils.toJson(header, true));
        assertThat(strip(getZarrayContent(fooPath)), is(equalToIgnoringWhiteSpace(expected)));
        assertThat(getZattrsContent(fooPath), is("{\"data\":[4.0,5.0,6.0,7.0,8.0]}"));
    }

    @Test
    public void createSubGroup() throws IOException, InvalidRangeException {
        //preparation
        final Map<String, Object> attributes = TestUtils.createMap("aaaa", "pfrt", "y", 123);

        //execution
        final ZarrGroup rootGrp = ZarrGroup.create(store, null);
        final ZarrGroup foo = rootGrp.createGroup("foo", attributes);

        //verification
        final Path fooPath = rootPath.resolve("foo");
        assertThat(Files.isDirectory(fooPath), is(true));
        assertThat(Files.list(fooPath).count(), is(2L));
        assertThat(Files.isReadable(fooPath.resolve(FILENAME_DOT_ZGROUP)), is(true));
        assertThat(Files.isReadable(fooPath.resolve(FILENAME_DOT_ZATTRS)), is(true));
        assertThat(getZgroupContent(fooPath), is("{\"zarr_format\":2}"));
        assertThat(getZattrsContent(fooPath), is("{\"y\":123,\"aaaa\":\"pfrt\"}"));
    }

    //    @Test
    public void name() throws IOException, InvalidRangeException {
        //preparation
        final int[] shape = {10, 10};
        final int[] chunks = {5, 5};
        final byte[] data = new byte[100];
        Arrays.fill(data, (byte) 42);
        final Map<String, Object> attr1 = TestUtils.createMap("lsmf", 345, "menno", 23.23);
        final Map<String, Object> attr2 = TestUtils.createMap("aaaa", "pfrt", "y", 123);
        final Map<String, Object> attr3 = TestUtils.createMap("data", new double[]{4, 5, 6, 7, 8});

        //execution
        final ZarrGroup rootGrp = ZarrGroup.create(store, attr1);
        ZarrGroup fooGrp = rootGrp.createGroup("foo", attr2);
        final ZarrArray barArray = fooGrp.createArray("bar", ZarrDataType.i1, shape, chunks, 0, null, attr3);
        barArray.write(data, shape, new int[]{0, 0});

        //verification
        final Map<String, Path> paths = new HashMap<>();
        final Path zGrpPath;

        assertThat(Files.isDirectory(rootPath), is(true));
        assertThat(Files.isReadable(rootPath.resolve(FILENAME_DOT_ZGROUP)), is(true));

        /* Files in store root dir */
        Files.list(rootPath).forEach(path -> paths.put(path.getFileName().toString(), path));
        assertThat(paths.size(), is(3));
        assertThat(paths.containsKey("foo"), is(true));
        assertThat(paths.containsKey(".zgroup"), is(true));
        assertThat(paths.containsKey(".zattrs"), is(true));

        assertThat(Files.isDirectory(paths.get("foo")), is(true));

        zGrpPath = paths.get(".zgroup");
        assertThat(Files.isReadable(zGrpPath), is(true));
        final String content = TestUtils.readContent(zGrpPath);
        assertThat(content, is("{\"zarr_format\":2}"));
        assertThat(Files.isReadable(zGrpPath), is(true));
    }

    private String strip(String s) {
        s = s.replace("\r", "").replace("\n", "");
        s = s.replace(" ", "");
//        while (s.contains("  ")) s = s.replace("  ", " ");
        return s;
    }


}