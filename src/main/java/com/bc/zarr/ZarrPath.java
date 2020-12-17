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

package com.bc.zarr;

class ZarrPath {
    final String storeKey;

    ZarrPath(String storeKey) {
        if ("".equals(storeKey)) {
            this.storeKey = storeKey;
            return;
        }

        this.storeKey = normalizeStoragePath(storeKey);
    }

    ZarrPath resolve(String name) {
        name = normalizeStoragePath(name);
        return new ZarrPath(storeKey + "/" + name);
    }

    private static String normalizeStoragePath(String path) {

        //replace backslashes with slashes
        path = path.replace("\\", "/");

        // collapse any repeated slashes
        while (path.contains("//")) {
            path = path.replace("//", "/");
        }

        // ensure no leading slash
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        // ensure no trailing slash
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        // don't allow path segments with just '.' or '..'
        final String[] split = path.split("/");
        for (String s : split) {
            s = s.trim();
            if (".".equals(s) || "..".equals(s)) {
                throw new IllegalArgumentException("path containing '.' or '..' segment not allowed");
            }
        }
        return path;
    }
}
