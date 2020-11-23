package com.bc.zarr;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        map.put("id", "zlib");
        map.put("level", 1);
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
     * @param id the type of the compression algorithm
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
            if (levelObj instanceof String) {
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
}

