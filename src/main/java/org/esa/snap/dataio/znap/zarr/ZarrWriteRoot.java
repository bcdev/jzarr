package org.esa.snap.dataio.znap.zarr;

import static org.esa.snap.dataio.znap.zarr.ZarrConstantsAndUtils.*;

import org.apache.commons.lang.ArrayUtils;
import org.esa.snap.core.util.io.TreeDeleter;
import org.esa.snap.dataio.znap.zarr.chunk.Compressor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        final Path dataPath = this.rootPath.resolve(rastername);

        if (Files.isDirectory(dataPath)) {
            TreeDeleter.deleteDir(dataPath);
        }
        Files.createDirectories(dataPath);

        final ZarrHeader zarrHeader = new ZarrHeader(shape, chunks, dataType.toString(), fillValue, compressor);
        writeJson(dataPath, FILENAME_DOT_ZARRAY, zarrHeader);
        writeAttributes(dataPath, attributes);

        return new ZarrReaderWriter(dataPath, shape, chunks, dataType, fillValue, compressor);
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
