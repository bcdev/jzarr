package com.bc.zarr;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class CompressorFactory {

    public final static Compressor nullCompressor = new NullCompressor();

    public static Compressor create(String id, int level) {
        if ("null".equals(id)) {
            return nullCompressor;
        }
        if ("zlib".equals(id)) {
            return new ZlibCompressor(level);
        }
        throw new IllegalStateException("Compressor id:'" + id + "' level:'" + level + "' unknown.");
    }

    private static class NullCompressor extends Compressor {
        @Override
        public String getId() {
            return null;
        }

        @Override
        public int getLevel() {
            return 0;
        }

        @Override
        public void compress(InputStream is, OutputStream os) throws IOException {
            passThrough(is, os);
        }

        @Override
        public void uncompress(InputStream is, OutputStream os) throws IOException {
            passThrough(is, os);
        }
    }

    private static class ZlibCompressor extends Compressor {
        private final int level;

        private ZlibCompressor(int level) {
            this.level = level;
        }

        @Override
        public String getId() {
            return "zlib";
        }

        @Override
        public int getLevel() {
            return level;
        }

        @Override
        public void compress(InputStream is, OutputStream os) throws IOException {
            try (final DeflaterOutputStream dos = new DeflaterOutputStream(os, new Deflater(level))) {
                passThrough(is, dos);
            }
        }

        @Override
        public void uncompress(InputStream is, OutputStream os) throws IOException {
            try (final InflaterInputStream iis = new InflaterInputStream(is, new Inflater())) {
                passThrough(iis, os);
            }
        }
    }

}

