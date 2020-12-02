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
