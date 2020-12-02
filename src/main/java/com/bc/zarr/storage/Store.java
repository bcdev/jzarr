/*
 *
 * Copyright (C) 2020 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.zarr.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

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

    Set<String> getArrayKeys() throws IOException;

    Set<String> getGroupKeys() throws IOException;
}
