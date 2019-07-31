package com.bc.zarr;

import com.bc.zarr.storage.FileSystemStore;
import com.bc.zarr.storage.InMemoryStore;
import com.bc.zarr.storage.Store;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static com.bc.zarr.ZarrConstants.*;

public class ZarrGroup {

    /**
     * @param path
     * @param attributes
     * @throws IOException
     */
    public static ZarrGroup create(String path, final Map<String, Object> attributes) throws IOException {
        final Store store;
        if (path == null) {
            store = new InMemoryStore();
        } else {
            store = new FileSystemStore(Paths.get(path));
        }

        return create(store, attributes);
    }

    public static ZarrGroup create(Store store, final Map<String, Object> attributes) throws IOException {
        ZarrGroup zarrGroup = new ZarrGroup(store);
        zarrGroup.createHeader();
        zarrGroup.writeAttributes(attributes);
        return zarrGroup;
    }

    public static ZarrGroup open(Path groupPath) throws IOException {
        ZarrUtils.ensureDirectory(groupPath);
        return open(new FileSystemStore(groupPath));
    }

    public static ZarrGroup open(Store store) throws IOException {
        try (InputStream is = store.getInputStream(FILENAME_DOT_ZGROUP)) {
            if (is == null) {
                throw new IOException("'" + FILENAME_DOT_ZGROUP + "' expected but is not readable or missing in store.");
            }
            ensureZarrFormatIs2(is);
        }
        return new ZarrGroup(store);
    }

    private static void ensureZarrFormatIs2(InputStream is) throws IOException {

        try (
                final InputStreamReader in = new InputStreamReader(is);
                BufferedReader reader = new BufferedReader(in)
        ) {
            final ZarrFormat fromJson = ZarrUtils.fromJson(reader, ZarrFormat.class);
            if (fromJson.zarr_format != 2) {
                throw new IOException("Zarr format 2 expected but is '" + fromJson.zarr_format + "'");
            }
        }
    }

    public ZarrGroup createGroup(String subGroupKey, Map<String, Object> attributes) throws IOException {
        final ZarrGroup group = new ZarrGroup(subGroupKey, store);
        group.createHeader();
        group.writeAttributes(attributes);
        return group;
    }

    private final class ZarrFormat {
        double zarr_format;
    }

    private final Store store;
    private final ZarrPath zarrPath;

    private ZarrGroup(Store store) {
        this.zarrPath = new ZarrPath("");
        this.store = store;
    }

    private ZarrGroup(String subGroupKey, Store store) {
        this.zarrPath = new ZarrPath(subGroupKey);
        this.store = store;
    }

    public ZarrArray createArray(String name, ZarrDataType dataType, int[] shape, int[] chunks, Number fillValue, Compressor compressor, final Map<String, Object> attributes) throws IOException {
        final ZarrPath relativePath = zarrPath.resolve(name);
        return ZarrArray.create(relativePath, store, shape, chunks, dataType, fillValue, compressor, attributes);
    }

    public ZarrArray openArray(String name) throws IOException {
        return ZarrArray.open(zarrPath.resolve(name), store);
    }

    private void createHeader() throws IOException {
        final Map<String, Integer> singletonMap = Collections.singletonMap(ZARR_FORMAT, 2);
        final ZarrPath headerPath = zarrPath.resolve(FILENAME_DOT_ZGROUP);
        try (
                final OutputStream os = store.getOutputStream(headerPath.storeKey);
                final OutputStreamWriter writer = new OutputStreamWriter(os)
        ) {
            ZarrUtils.toJson(singletonMap, writer);
        }
    }

    private void writeAttributes(Map<String, Object> attributes) throws IOException {
        ZarrUtils.writeAttributes(attributes, zarrPath, store);
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
