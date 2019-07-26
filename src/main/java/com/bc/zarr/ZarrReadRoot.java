package com.bc.zarr;

import java.nio.file.Path;

public class ZarrReadRoot {

    private final Path _rootPath;

    public ZarrReadRoot(Path rootPath)  {
        _rootPath = rootPath;
    }

    public ArrayDataReader create(String name, ZarrDataType dataType, int[] shape, int[] chunks, Number fillValue, Compressor compressor) {
        final Path dataPath = _rootPath.resolve(name);
        return new ArrayDataReaderWriter(dataPath, shape, chunks, dataType, fillValue, compressor);
    }
}
