package com.bc.zarr;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class Compressor {

    public abstract String getId();

    public abstract int getLevel();

    public abstract void compress(InputStream is, OutputStream os) throws IOException;

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
