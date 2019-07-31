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
