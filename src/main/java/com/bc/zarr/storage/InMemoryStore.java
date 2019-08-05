package com.bc.zarr.storage;

import com.bc.zarr.ZarrConstants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

public class InMemoryStore implements Store {
    private final Map<String, byte[]> map = new HashMap<>();

    @Override
    public InputStream getInputStream(String key) {
        final byte[] bytes = map.get(key);
        if (bytes != null) {
            return new ByteArrayInputStream(bytes);
        } else {
            return null;
        }
    }

    @Override
    public OutputStream getOutputStream(String key) {
        return new ByteArrayOutputStream() {
            @Override
            public void close() {
                map.remove(key);
                map.put(key, this.toByteArray());
            }
        };
    }

    @Override
    public void delete(String key) {
        map.remove(key);
    }

    @Override
    public TreeSet<String> getArrayKeys() {
        return getKeysFor(ZarrConstants.FILENAME_DOT_ZARRAY);
    }

    @Override
    public TreeSet<String> getGroupKeys() {
        return getKeysFor(ZarrConstants.FILENAME_DOT_ZGROUP);
    }

    private TreeSet<String> getKeysFor(String suffix) {
        final Set<String> keySet = map.keySet();
        final TreeSet<String> arrayKeys = new TreeSet<>();
        for (String key : keySet) {
            if(key.endsWith(suffix)) {
                String relKey = key.replace(suffix, "");
                while (relKey.endsWith("/")) relKey= relKey.substring(0, relKey.length()-1);
                arrayKeys.add(relKey);
            }
        }
        return arrayKeys;
    }
}
