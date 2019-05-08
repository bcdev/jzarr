package org.esa.snap.dataio.znap.zarr.chunk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public enum Compressor {
    Null("Null", 0) {
        @Override
        public void compress(InputStream is, OutputStream os) throws IOException {
            passThrough(is, os);
        }

        @Override
        public void uncompress(InputStream is, OutputStream os) throws IOException {
            passThrough(is, os);
        }
    },
    Zip_L1("zlib", 1) {
        final boolean nowrap = false;

        @Override
        public void compress(InputStream is, OutputStream os) throws IOException {
            try (final DeflaterOutputStream dos = new DeflaterOutputStream(os, new Deflater(getLevel(), nowrap))) {
                passThrough(is, dos);
            }
        }

        @Override
        public void uncompress(InputStream is, OutputStream os) throws IOException {
            try (final InflaterInputStream iis = new InflaterInputStream(is, new Inflater(nowrap))) {
                passThrough(iis, os);
            }
        }
    };

    protected final String id;
    protected final int level;

    Compressor(String id, int level) {
        this.id = id;
        this.level = level;
    }

    public String getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public abstract void compress(InputStream is, OutputStream os) throws IOException;

    public abstract void uncompress(InputStream is, OutputStream os) throws IOException;

    protected void passThrough(InputStream is, OutputStream os) throws IOException {
        final byte[] bytes = new byte[4096];
        int read = is.read(bytes);
        while (read > 0) {
            os.write(bytes, 0, read);
            read = is.read(bytes);
        }
    }
}
