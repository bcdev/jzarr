package com.bc.zarr;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class Compressor {

    public abstract String getId();

    public abstract int getLevel();

    /**
     * Compresses data from InputStream is and writes to OutputStream os. DataType and chunkSize are used only in case of Blosc compressor
     * @param   is  InputStream from which uncompressed data is read
     * @param   os  OutputStream where compressed data is written to
     * @param   dataType    data type to determine its size, used only in case of Blosc compressor
     * @param   chunkSize   number of elements in the chunk to be compressed, used only in case of Blosc compressor
     */
    public abstract void compress(InputStream is, OutputStream os, ucar.ma2.DataType dataType, int chunkSize) throws IOException;

    public abstract void uncompress(InputStream is, OutputStream os) throws IOException;

    void passThrough(InputStream is, OutputStream os) throws IOException {
        final byte[] bytes = new byte[4096];
        int read = is.read(bytes);
        while (read > 0) {
            os.write(bytes, 0, read);
            read = is.read(bytes);
        }
    }
}
