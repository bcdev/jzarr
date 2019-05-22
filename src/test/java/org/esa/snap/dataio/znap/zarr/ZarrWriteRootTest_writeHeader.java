package org.esa.snap.dataio.znap.zarr;

import static org.esa.snap.dataio.znap.zarr.ZarrConstantsAndUtils.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.esa.snap.dataio.znap.zarr.chunk.Compressor;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.*;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ZarrWriteRootTest_writeHeader {

    private FileSystem fs;
    private Path zarr_product_root;
    private ZarrWriteRoot zarrWriteRoot;

    @org.junit.Before
    public void setUp() throws Exception {
        fs = Jimfs.newFileSystem(Configuration.windows());
        final Iterable<Path> rootDirectories = fs.getRootDirectories();
        zarr_product_root = rootDirectories.iterator().next().resolve("zarr_product_root");
        zarrWriteRoot = new ZarrWriteRoot(zarr_product_root);
    }

    @org.junit.After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCreateHeaderFile() throws IOException {
        //preparation
        final String rastername = "Band4321";
        final String dataType = "i4";
        final int[] shape = {10, 15};
        final int[] chunks = {6, 8};
        final Number fillValue = 0;

        //execution
        zarrWriteRoot.create(rastername, ZarrDataType.i4, shape, chunks, fillValue, Compressor.Null, null);

        //verification
        final Path json_file = zarr_product_root.resolve(rastername).resolve(".zarray");
        assertThat(Files.isRegularFile(json_file), is(true));
        final String expectedJson = ZarrConstantsAndUtils.toJson(new ZarrHeader(shape, chunks, dataType, fillValue, Compressor.Null));
        final String fromFile = new String(Files.readAllBytes(json_file));
        assertThat(fromFile, is(equalToIgnoringWhiteSpace(expectedJson)));

        final List<Path> paths = Files.list(json_file.getParent()).collect(Collectors.toList());
        assertEquals(1, paths.size());
        assertEquals("C:\\zarr_product_root\\Band4321\\.zarray", paths.get(0).toString());
    }

    @Test
    public void testCreateHeaderFileAndAttributes() throws IOException {
        //preparation
        final String rastername = "Band4321";
        final String dataType = "i4";
        final int[] shape = {10, 15};
        final int[] chunks = {6, 8};
        final Number fillValue = 0;

        final HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("some", "new");
        attributes.put("with", "count");
        attributes.put("of", 3.0);

        //execution
        zarrWriteRoot.create(rastername, ZarrDataType.i4, shape, chunks, fillValue, Compressor.Null, attributes);

        //verification
        final Path zarray_file = zarr_product_root.resolve(rastername).resolve(FILENAME_DOT_ZARRAY);
        final Path zattrs_file = zarr_product_root.resolve(rastername).resolve(FILENAME_DOT_ZATTRS);
        assertThat(Files.isRegularFile(zarray_file), is(true));
        assertThat(Files.isRegularFile(zattrs_file), is(true));

        final String expectedJson = ZarrConstantsAndUtils.toJson(new ZarrHeader(shape, chunks, dataType, fillValue, Compressor.Null));
        final String fromFile = new String(Files.readAllBytes(zarray_file));
        assertThat(fromFile, is(equalToIgnoringWhiteSpace(expectedJson)));

        final Map zattrs = fromJson(Files.newBufferedReader(zattrs_file), Map.class);
        assertThat(zattrs.size(), is(equalTo(attributes.size())));
        for (String key : attributes.keySet()) {
            assertThat("Must contain key '" + key + "'", zattrs.containsKey(key), is(true));
            assertThat(zattrs.get(key), is(equalTo(attributes.get(key))));
        }

        final List<Path> paths = Files.list(zarray_file.getParent()).collect(Collectors.toList());
        assertEquals(2, paths.size());
        assertEquals("C:\\zarr_product_root\\Band4321\\.zarray", paths.get(0).toString());
        assertEquals("C:\\zarr_product_root\\Band4321\\.zattrs", paths.get(1).toString());
    }
}