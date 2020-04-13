package com.bc.zarr;

import org.blosc.BufferSizes;
import org.blosc.JBlosc;
import org.blosc.Shuffle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
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
        if ("blosc".equals(id)) {
            return new BloscCompressor(level);
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
        public void compress(InputStream is, OutputStream os, ucar.ma2.DataType dataType, int chunkSize) throws IOException {
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
        public void compress(InputStream is, OutputStream os, ucar.ma2.DataType dataType, int chunkSize) throws IOException {
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

    private static class BloscCompressor extends Compressor {
        private final int clevel;

        private BloscCompressor(int clevel) {
            this.clevel = clevel;
        }

        @Override
        public String getId() {
            return "blosc";
        }

        @Override
        public int getLevel() {
            return clevel;
        }

        @Override
        public void compress(InputStream is, OutputStream os, ucar.ma2.DataType dataType, int chunkSize) throws IOException {
            byte[] inChunkBytes = new byte[chunkSize * dataType.getSize()];
            int readBytes = is.read(inChunkBytes);
            while (readBytes > 0) {
                JBlosc jb = new JBlosc();
                jb.setCompressor("zstd");
                ByteBuffer outBuffer = ByteBuffer.allocate(readBytes + JBlosc.OVERHEAD);
                jb.compress(clevel, Shuffle.BYTE_SHUFFLE, dataType.getSize(), ByteBuffer.wrap(inChunkBytes), readBytes, outBuffer, readBytes + JBlosc.OVERHEAD);

                BufferSizes bs = jb.cbufferSizes(outBuffer);
                byte[] compressedChunk = Arrays.copyOfRange(outBuffer.array(), 0, (int) bs.getCbytes());

                os.write(compressedChunk);
                readBytes = is.read(inChunkBytes);

                os.flush();
                os.close();
                jb.destroy();
            }

            is.close();
        }

        @Override
        public void uncompress(InputStream is, OutputStream os) throws IOException {
            while (is.available() > 0) {
                byte[] header = new byte[16];
                is.read(header);

                JBlosc jb = new JBlosc();
                BufferSizes bs = jb.cbufferSizes(ByteBuffer.wrap(header));
                int chunkSize = (int) bs.getCbytes();

                byte[] inBytes = Arrays.copyOf(header, header.length + chunkSize);
                is.read(inBytes, header.length, chunkSize);

                ByteBuffer outBuffer = ByteBuffer.allocate((int) bs.getNbytes());
                jb.decompress(ByteBuffer.wrap(inBytes), outBuffer, outBuffer.limit());

                os.write(outBuffer.array());
                os.flush();
                os.close();
                jb.destroy();
            }
            is.close();
        }
    }
}

