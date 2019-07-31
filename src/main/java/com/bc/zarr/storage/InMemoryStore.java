package com.bc.zarr.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

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
}
