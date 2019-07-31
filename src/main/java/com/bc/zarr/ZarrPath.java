package com.bc.zarr;

public class ZarrPath {
    final String storeKey;

    public ZarrPath(String storeKey) {
        if ("".equals(storeKey)) {
            this.storeKey = storeKey;
            return;
        }

        this.storeKey = ZarrUtils.normalizeStoragePath(storeKey);
    }

    ZarrPath resolve(String name) {
        name = ZarrUtils.normalizeStoragePath(name);
        return new ZarrPath(storeKey + "/" + name);
    }

}
