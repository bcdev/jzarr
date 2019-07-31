package com.bc.zarr.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Store interface according to https://zarr.readthedocs.io/en/stable/spec/v2.html#storage
 * A Zarr array can be stored in any storage system that provides a key/value interface, where
 * a key is an ASCII string and a value is an arbitrary sequence of bytes, and the supported
 * operations are read (get the sequence of bytes associated with a given key), write (set the
 * sequence of bytes associated with a given key) and delete (remove a key/value pair).
 */
public interface Store {

    InputStream getInputStream(String key) throws IOException;

    OutputStream getOutputStream(String key) throws IOException;

    void delete(String key) throws IOException;
}
