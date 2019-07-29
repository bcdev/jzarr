package com.bc.zarr;

import java.io.InputStream;
import java.io.OutputStream;

public class DirectoryStore implements Storage {

    @Override
    public InputStream read(String key) {
        synchronized ()
        return null;
    }

    @Override
    public OutputStream write(String key) {
        return null;
    }

    @Override
    public boolean delete(String key) {
        return false;
    }
}
