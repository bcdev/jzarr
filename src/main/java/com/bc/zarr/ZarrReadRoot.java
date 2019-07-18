package com.bc.zarr;

import java.nio.file.Path;

public class ZarrReadRoot {

    private final Path _rootPath;

    public ZarrReadRoot(Path rootPath)  {
        _rootPath = rootPath;
    }

    public ZarrReader create(String rastername, ZarrDataType dataType, int[] shape, int[] chunks, Number fillValue, Compressor compressor) {
        final Path dataPath = _rootPath.resolve(rastername);
        return new ZarrReaderWriter(dataPath, shape, chunks, dataType, fillValue, compressor);
    }
}
