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
        final Object root = TestUtils.getPrivateFieldObject(store, "root");
        assertThat(root, is(instanceOf(Path.class)));
        assertThat(root.toString(), is("lsmf"));
    }

}