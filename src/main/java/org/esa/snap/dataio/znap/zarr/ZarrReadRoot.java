package org.esa.snap.dataio.znap.zarr;

import org.esa.snap.dataio.znap.zarr.chunk.Compressor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class ZarrReadRoot {

    private final Path _rootPath;

    public ZarrReadRoot(Path rootPath) throws IOException {
        _rootPath = rootPath;
    }

    public ZarrReader create(String rastername, ZarrDataType dataType, int[] shape, int[] chunks, Number fillValue, Compressor compressor, final Map<String, Object> attributes) throws IOException {
        final Path dataPath = _rootPath.resolve(rastername);
        return new ZarrReaderWriter(dataPath, shape, chunks, dataType, fillValue, compressor);
    }
}
