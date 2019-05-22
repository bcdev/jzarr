package org.esa.snap.dataio.znap.zarr;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.esa.snap.dataio.znap.zarr.chunk.Compressor;
import org.junit.*;

import java.lang.reflect.Field;
import java.nio.file.Paths;

public class ZarrReadRootTest {

    @Test
    public void create() throws NoSuchFieldException, IllegalAccessException {
        final ZarrReadRoot readRoot = new ZarrReadRoot(Paths.get("lsmf"));
        final ZarrReader reader = readRoot.create("rastername", ZarrDataType.f4, new int[]{101, 102}, new int[]{11, 12}, 4.2, Compressor.Zip_L1);

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