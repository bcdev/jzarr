package com.bc.zarr;

import com.bc.zarr.chunk.Compressor;
import com.google.common.jimfs.Jimfs;
import org.esa.snap.core.util.io.TreeDeleter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ZarrReadRootTest {

    private Path rootPath;

    @Before
    public void setUp() throws Exception {
        rootPath = Jimfs.newFileSystem().getPath("lsmf");
    }

    @Test
    public void create() throws NoSuchFieldException, IllegalAccessException, IOException {
        final ZarrGroup readRoot = ZarrGroup.open(rootPath);
        final ZarrWriter reader = readRoot.createWriter("rastername", ZarrDataType.f4, new int[]{101, 102}, new int[]{11, 12}, 4.2, Compressor.zlib_L1, null);

        assertThat(reader, is(instanceOf(ZarrReaderWriter.class)));

        final ZarrReaderWriter zarrReaderWriter = (ZarrReaderWriter) reader;
        final String name = "_dataPath";
        final Object path = getPrivateFieldObject(zarrReaderWriter, name);
        assertThat(path.toString(), is("lsmf\\rastername"));
    }

    private Object getPrivateFieldObject(ZarrReaderWriter zarrReaderWriter, String name) throws NoSuchFieldException, IllegalAccessException {
        final Field dataPath = zarrReaderWriter.getClass().getDeclaredField(name);
        dataPath.setAccessible(true);
        return dataPath.get(zarrReaderWriter);
    }
}