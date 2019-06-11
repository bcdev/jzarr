package com.bc.zarr;


import static com.bc.zarr.ZarrConstantsAndUtils.*;

import com.bc.zarr.chunk.Compressor;
import org.apache.commons.lang.ArrayUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Map;

public class ZarrWriteRoot {

    private final Path rootPath;

    public ZarrWriteRoot(Path rootPath) throws IOException {
        this(rootPath, null);
    }

    public ZarrWriteRoot(Path rootPath, final Map<String, Object> productAttributes) throws IOException {
        this.rootPath = rootPath;
        Files.createDirectories(rootPath);

        writeJson(rootPath, FILENAME_DOT_ZGROUP, ArrayUtils.toMap(new Object[][]{{ZARR_FORMAT, 2}}));
        writeAttributes(rootPath, productAttributes);
    }

    public ZarrWriter create(String rastername, ZarrDataType dataType, int[] shape, int[] chunks, Number fillValue, Compressor compressor, final Map<String, Object> attributes) throws IOException {
        final Path dataPath = initialize(rastername);

        final ZarrHeader zarrHeader = new ZarrHeader(shape, chunks, dataType.toString(), fillValue, compressor);
        writeJson(dataPath, FILENAME_DOT_ZARRAY, zarrHeader);
        writeAttributes(dataPath, attributes);

        return new ZarrReaderWriter(dataPath, shape, chunks, dataType, fillValue, compressor);
    }

    private Path initialize(String rastername) throws IOException {
        final Path dataPath = this.rootPath.resolve(rastername);

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
        if (exceptions.size()>0) {
            final IOException ioException = new IOException(String.format("Unable to initialize the storage for raster '%s'", rastername), exceptions.get(0));
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
                toJson(object, writer);
            }
        }
    }
}
