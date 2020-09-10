package com.bc.zarr;

import static com.bc.zarr.ZarrUtils.*;
import static com.bc.zarr.ZarrConstants.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.bc.zarr.storage.FileSystemStore;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.*;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ZarrWriteRootTest_writeHeader {

    private Path zarr_product_root;
    private ZarrGroup zarrGroup;

    @org.junit.Before
    public void setUp() throws Exception {
        final FileSystem fs = Jimfs.newFileSystem(Configuration.windows());
        final Iterable<Path> rootDirectories = fs.getRootDirectories();
        zarr_product_root = rootDirectories.iterator().next().resolve("zarr_product_root");
        final FileSystemStore store = new FileSystemStore(zarr_product_root);
        zarrGroup = ZarrGroup.create(store, null);
    }

    @Test
    public void testCreateHeaderFile() throws IOException, JZarrException {
        //preparation
        final String rastername = "Band4321";
        final String dataType = "i4";
        final int[] shape = {10, 15};
        final int[] chunks = {6, 8};
        final Number fillValue = 0;
        final Compressor compressor = CompressorFactory.create("zlib", 1);
        final ArrayParams parameters = new ArrayParams()
                .dataType(DataType.i4)
                .shape(shape)
                .chunks(chunks)
                .fillValue(fillValue).compressor(compressor);

        //execution
        zarrGroup.createArray(rastername, parameters, null);

        //verification
        final Path json_file = zarr_product_root.resolve(rastername).resolve(".zarray");
        assertThat(Files.isRegularFile(json_file), is(true));
        final String expectedJson = ZarrUtils.toJson(new ZarrHeader(shape, chunks, dataType,ByteOrder.BIG_ENDIAN, fillValue, compressor), true);
        final String fromFile = new String(Files.readAllBytes(json_file));
        assertThat(fromFile, is(equalToIgnoringWhiteSpace(expectedJson)));

        final List<Path> paths = Files.list(json_file.getParent()).collect(Collectors.toList());
        assertEquals(1, paths.size());
        assertEquals("C:\\zarr_product_root\\Band4321\\.zarray", paths.get(0).toString());
    }

    @Test
    public void testCreateHeaderFileAndAttributes() throws IOException, JZarrException {
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
        final ArrayParams parameters = new ArrayParams()
                .dataType(DataType.i4)
                .shape(shape)
                .chunks(chunks)
                .fillValue(fillValue)
                .compressor(null);

        //execution
        zarrGroup.createArray(rastername, parameters, attributes);

        //verification
        final Path zarray_file = zarr_product_root.resolve(rastername).resolve(FILENAME_DOT_ZARRAY);
        final Path zattrs_file = zarr_product_root.resolve(rastername).resolve(FILENAME_DOT_ZATTRS);
        assertThat(Files.isRegularFile(zarray_file), is(true));
        assertThat(Files.isRegularFile(zattrs_file), is(true));

        final Compressor nullCompressor = CompressorFactory.nullCompressor;
        final String expectedJson = ZarrUtils.toJson(new ZarrHeader(shape, chunks, dataType, ByteOrder.BIG_ENDIAN, fillValue, nullCompressor), true);
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