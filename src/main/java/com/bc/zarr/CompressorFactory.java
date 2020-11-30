package com.bc.zarr;

import com.sun.jna.ptr.NativeLongByReference;
import org.blosc.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class CompressorFactory {

    public final static Compressor nullCompressor = new NullCompressor();

    /**
     * @return the properties of the default compressor as a key/value map.
     */
    public static Map<String, Object> getDefaultCompressorProperties() {
        final Map<String, Object> map = new HashMap<>();

        /* zlib defaults */
//        map.put("id", "zlib");
//        map.put("level", 1);

        /* blosc defaults */
        map.put("id", "blosc");
        map.put("cname", "lz4");
        map.put("clevel", 5);
        map.put("blocksize", 0);
        map.put("shuffle", 1);

        return map;
    }

    /**
     * @return a new Compressor instance using the method {@link #create(Map properties)} with {@link #getDefaultCompressorProperties()}.
     */
    public static Compressor createDefaultCompressor() {
        return create(getDefaultCompressorProperties());
    }

    /**
     * Creates a new {@link Compressor} instance according to the given properties.
     *
     * @param properties a Map containing the properties to create a compressor
     * @return a new Compressor instance according to the properties
     * @throws IllegalArgumentException If it is not able to create a Compressor.
     */
    public static Compressor create(Map<String, Object> properties) {
        final String id = (String) properties.get("id");
        return create(id, properties);
    }

    /**
     * Creates a new {@link Compressor} instance according to the id and the given properties.
     *
     * @param id           the type of the compression algorithm
     * @param keyValuePair an even count of key value pairs defining the compressor specific properties
     * @return a new Compressor instance according to the id and the properties
     * @throws IllegalArgumentException If it is not able to create a Compressor.
     */
    public static Compressor create(String id, Object... keyValuePair) {
        if (keyValuePair.length % 2 != 0) {
            throw new IllegalArgumentException("The count of keyValuePair arguments must be an even count.");
        }
        return create(id, toMap(keyValuePair));
    }

    /**
     * Creates a new {@link Compressor} instance according to the id and the given properties.
     *
     * @param id         the type of the compression algorithm
     * @param properties a Map containing the compressor specific properties
     * @return a new Compressor instance according to the id and the properties
     * @throws IllegalArgumentException If it is not able to create a Compressor.
     */
    public static Compressor create(String id, Map<String, Object> properties) {
        if ("null".equals(id)) {
            return nullCompressor;
        }
        if ("zlib".equals(id)) {
            return new ZlibCompressor(properties);
        }
        if ("blosc".equals(id)) {
            return new BloscCompressor(properties);
        }
        throw new IllegalArgumentException("Compressor id:'" + id + "' not supported.");
    }

    private static Map<String, Object> toMap(Object... args) {
        final HashMap<String, Object> map = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            String key = (String) args[i];
            Object val = args[i + 1];
            map.put(key, val);
        }
        return map;
    }

    private static class NullCompressor extends Compressor {

        @Override
        public String getId() {
            return null;
        }

        @Override
        public String toString() {
            return getId();
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

        private ZlibCompressor(Map<String, Object> map) {
            final Object levelObj = map.get("level");
            if (levelObj == null) {
                this.level = 1; //default value
            } else if (levelObj instanceof String) {
                this.level = Integer.parseInt((String) levelObj);
            } else {
                this.level = ((Number) levelObj).intValue();
            }
            validateLevel();
        }

        @Override
        public String toString() {
            return "compressor=" + getId() + "/level=" + level;
        }

        private void validateLevel() {
            // see new Deflater().setLevel(level);
            if (level < 0 || level > 9) {
                throw new IllegalArgumentException("Invalid compression level: " + level);
            }
        }

        @Override
        public String getId() {
            return "zlib";
        }

        // this getter is needed for JSON serialisation
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

    private static class BloscCompressor extends Compressor {
        private final int clevel;
        private final int blocksize;
        private final int shuffle;
        private final String cname;

        private BloscCompressor(Map<String, Object> map) {
            final Object cnameObj = map.get("cname");
            if (cnameObj == null) {
                cname = "lz4"; //default value
            } else {
                cname = (String) cnameObj;
            }
            final String[] supportedNames = {"zstd", "blosclz", "lz4", "lz4hc", "zlib", "snappy"};
            if (Arrays.stream(supportedNames).noneMatch(cname::equals)) {
                throw new IllegalArgumentException(
                        "blosc: compressor not supported: '" + cname + "'; expected one of " + Arrays.toString(supportedNames));
            }

            final Object clevelObj = map.get("clevel");
            if (clevelObj == null) {
                clevel = 5; //default value
            } else if (clevelObj instanceof String) {
                clevel = Integer.parseInt((String) clevelObj);
            } else {
                clevel = ((Number) clevelObj).intValue();
            }
            if (clevel < 0 || clevel > 9) {
                throw new IllegalArgumentException("blosc: clevel parameter must be between 0 and 9 but was: " + clevel);
            }

            final int AUTOSHUFFLE = -1;
            final int NOSHUFFLE = 0;
            final int BYTESHUFFLE = 1;
            final int BITSHUFFLE = 2;

            final Object shuffleObj = map.get("shuffle");
            if (shuffleObj == null) {
                this.shuffle = BYTESHUFFLE; //default value
            } else if (shuffleObj instanceof String) {
                this.shuffle = Integer.parseInt((String) shuffleObj);
            } else {
                this.shuffle = ((Number) shuffleObj).intValue();
            }
            final int[] supportedShuffle = new int[]{AUTOSHUFFLE, NOSHUFFLE, BYTESHUFFLE, BITSHUFFLE};
            final String[] supportedShuffleNames = new String[]{"-1 (AUTOSHUFFLE)", "0 (NOSHUFFLE)", "1 (BYTESHUFFLE)", "2 (BITSHUFFLE)"};
            if (Arrays.stream(supportedShuffle).noneMatch(value -> value == shuffle)) {
                throw new IllegalArgumentException(
                        "blosc: shuffle type not supported: '" + shuffle + "'; expected one of " + Arrays.toString(supportedShuffleNames));
            }

            final Object blocksizeObj = map.get("blocksize");
            if (blocksizeObj == null) {
                this.blocksize = 0; //default value
            } else if (blocksizeObj instanceof String) {
                this.blocksize = Integer.parseInt((String) blocksizeObj);
            } else {
                this.blocksize = ((Number) blocksizeObj).intValue();
            }
        }

        @Override
        public String getId() {
            return "blosc";
        }

        public int getClevel() {
            return clevel;
        }

        public int getBlocksize() {
            return blocksize;
        }

        public int getShuffle() {
            return shuffle;
        }

        public String getCname() {
            return cname;
        }

        @Override
        public String toString() {
            return "compressor=" + getId()
                   + "/cname=" + cname + "/clevel=" + clevel
                   + "/blocksize=" + blocksize + "/shuffle=" + shuffle;
        }

        @Override
        public void compress(InputStream is, OutputStream os) throws IOException {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            passThrough(is, baos);
            final byte[] inputBytes = baos.toByteArray();
            final int inputSize = inputBytes.length;
            final int outputSize = inputSize + JBlosc.OVERHEAD;
            final ByteBuffer inputBuffer = ByteBuffer.wrap(inputBytes);
            final ByteBuffer outBuffer = ByteBuffer.allocate(outputSize);
            final int i = JBlosc.compressCtx(clevel, shuffle, 1, inputBuffer, inputSize, outBuffer, outputSize, cname, blocksize, 1);
            final BufferSizes bs = cbufferSizes(outBuffer);
            byte[] compressedChunk = Arrays.copyOfRange(outBuffer.array(), 0, (int) bs.getCbytes());
            os.write(compressedChunk);
        }

        @Override
        public void uncompress(InputStream is, OutputStream os) throws IOException {
            while (is.available() >= 16) {
                byte[] header = new byte[16];
                is.read(header);

                BufferSizes bs = cbufferSizes(ByteBuffer.wrap(header));
                int chunkSize = (int) bs.getCbytes();

                byte[] inBytes = Arrays.copyOf(header, header.length + chunkSize);
                is.read(inBytes, header.length, chunkSize);

                ByteBuffer outBuffer = ByteBuffer.allocate((int) bs.getNbytes());
                JBlosc.decompressCtx(ByteBuffer.wrap(inBytes), outBuffer, outBuffer.limit(), 1);

                os.write(outBuffer.array());
            }
        }

        private BufferSizes cbufferSizes(ByteBuffer cbuffer) {
            NativeLongByReference nbytes = new NativeLongByReference();
            NativeLongByReference cbytes = new NativeLongByReference();
            NativeLongByReference blocksize = new NativeLongByReference();
            IBloscDll.blosc_cbuffer_sizes(cbuffer, nbytes, cbytes, blocksize);
            BufferSizes bs = new BufferSizes(nbytes.getValue().longValue(),
                                             cbytes.getValue().longValue(),
                                             blocksize.getValue().longValue());
            return bs;
        }
    }
}

