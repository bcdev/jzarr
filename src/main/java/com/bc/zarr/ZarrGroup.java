package com.bc.zarr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import static com.bc.zarr.ZarrConstants.*;

import static com.bc.zarr.CompressorFactory.nullCompressor;

public class ZarrGroup {

    public static ZarrGroup create(Path path, final Map<String, Object> attributes) throws IOException {
        Files.createDirectories(path);
        ZarrGroup zarrGroup = new ZarrGroup(path);
        zarrGroup.writeJson(path, FILENAME_DOT_ZGROUP, Collections.singletonMap(ZARR_FORMAT, 2));
        zarrGroup.writeAttributes(path, attributes);
        return zarrGroup;
    }

    public static ZarrGroup open(Path groupPath) throws IOException {
        ZarrUtils.ensureDirectory(groupPath);
        final Path dotZGroupPath = groupPath.resolve(FILENAME_DOT_ZGROUP);
        ZarrUtils.ensureFileExistAndIsReadable(dotZGroupPath);
        ensureZarrFormat2(dotZGroupPath);
        return new ZarrGroup(groupPath);
    }

    private static void ensureZarrFormat2(Path jsonPath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(jsonPath)) {
            final ZarrFormat fromJson = ZarrUtils.fromJson(reader, ZarrFormat.class);
            if (fromJson.zarr_format != 2) {
                throw new IOException("Zarr format 2 expected but is '" + fromJson.zarr_format + "'");
            }
        }
    }

    private final class ZarrFormat {
        double zarr_format;
    }

    private final Path path;

    private ZarrGroup(Path path) {
        this.path = path;
    }

    public ArrayDataWriter createWriter(String name, ZarrDataType dataType, int[] shape, int[] chunks, Number fillValue, Compressor compressor, final Map<String, Object> attributes) throws IOException {
        final Path dataPath = initialize(name);

        final ZarrHeader zarrHeader = new ZarrHeader(shape, chunks, dataType.toString(), fillValue, compressor);
        writeJson(dataPath, FILENAME_DOT_ZARRAY, zarrHeader);
        writeAttributes(dataPath, attributes);

        return new ArrayDataReaderWriter(dataPath, shape, chunks, dataType, fillValue, compressor);
    }

    public ArrayDataReader createReader(String name) throws IOException {
        final Path dataPath = path.resolve(name);
        final Path zarrHeaderPath = dataPath.resolve(FILENAME_DOT_ZARRAY);
        try (BufferedReader reader = Files.newBufferedReader(zarrHeaderPath)) {
            final ZarrHeader header = ZarrUtils.fromJson(reader, ZarrHeader.class);
            final int[] shape = header.getShape();
            final int[] chunks = header.getChunks();
            final ZarrDataType rawDataType = header.getRawDataType();
            final Number fill_value = header.getFill_value();
            final ZarrHeader.CompressorBean compressorBean = header.getCompressor();
            final Compressor compressor;
            if (compressorBean != null) {
                final String compId = compressorBean.getId();
                final int compLevel = compressorBean.getLevel();
                compressor = compressorBean != null ? CompressorFactory.create(compId, compLevel) : nullCompressor;
            } else {
                compressor = nullCompressor;
            }
            return new ArrayDataReaderWriter(dataPath, shape, chunks, rawDataType, fill_value, compressor);
        }
    }

    private Path initialize(String name) throws IOException {
        final Path dataPath = path.resolve(name);

        final LinkedList<IOException> exceptions = new LinkedList<>();
        if (Files.isDirectory(dataPath)) {
            Files.walk(dataPath).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    exceptions.add(e);
                }
            });
        }
        if (exceptions.size() > 0) {
            final IOException ioException = new IOException(String.format("Unable to initialize the storage for array data '%s'", name), exceptions.get(0));
            for (int i = 1; i < exceptions.size(); i++) {
                IOException e = exceptions.get(i);
                ioException.addSuppressed(e);
            }
            throw ioException;
        }

        Files.createDirectories(dataPath);
        return dataPath;
    }

    private void writeAttributes(Path dataPath, Map<String, Object> attributes) throws IOException {
        if (attributes != null && !attributes.isEmpty()) {
            writeJson(dataPath, FILENAME_DOT_ZATTRS, attributes);
        }
    }

    private void writeJson(Path dataPath, String filename, Object object) throws IOException {
        if (object != null) {
            final Path attrsPath = dataPath.resolve(filename);
            try (final BufferedWriter writer = Files.newBufferedWriter(attrsPath)) {
                ZarrUtils.toJson(object, writer);
            }
        }
    }
}
