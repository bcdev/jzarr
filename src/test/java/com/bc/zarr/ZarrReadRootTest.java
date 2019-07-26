package com.bc.zarr;

import com.google.common.jimfs.Jimfs;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static com.bc.zarr.ZarrConstants.FILENAME_DOT_ZGROUP;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
        final ZarrGroup readRoot = ZarrGroup.open(rootPath);
        final Compressor compressor = CompressorFactory.create("zlib", 1);
        final ArrayDataWriter reader = readRoot.createWriter("rastername", ZarrDataType.f4, new int[]{101, 102}, new int[]{11, 12}, 4.2, compressor, null);

        assertThat(reader, is(instanceOf(ArrayDataReaderWriter.class)));

        final ArrayDataReaderWriter zarrReaderWriter = (ArrayDataReaderWriter) reader;
        final String name = "_dataPath";
        final Object path = getPrivateFieldObject(zarrReaderWriter, name);
        assertThat(path.toString(), is("lsmf\\rastername"));
    }

    private Object getPrivateFieldObject(ArrayDataReaderWriter zarrReaderWriter, String name) throws NoSuchFieldException, IllegalAccessException {
        final Field dataPath = zarrReaderWriter.getClass().getDeclaredField(name);
        dataPath.setAccessible(true);
        return dataPath.get(zarrReaderWriter);
    }
}