/*
 *
 * MIT License
 *
 * Copyright (c) 2020. Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
