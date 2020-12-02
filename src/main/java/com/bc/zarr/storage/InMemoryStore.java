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
