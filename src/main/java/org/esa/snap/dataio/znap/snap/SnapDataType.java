package org.esa.snap.dataio.znap.snap;

import org.esa.snap.core.datamodel.ProductData;

public enum SnapDataType {
    TYPE_FLOAT64(ProductData.TYPE_FLOAT64),
    TYPE_FLOAT32(ProductData.TYPE_FLOAT32),
    TYPE_INT8(ProductData.TYPE_INT8),
    TYPE_UINT8(ProductData.TYPE_UINT8),
    TYPE_INT16(ProductData.TYPE_INT16),
    TYPE_UINT16(ProductData.TYPE_UINT16),
    TYPE_INT32(ProductData.TYPE_INT32),
    TYPE_UINT32(ProductData.TYPE_UINT32);

    private final int value;

    SnapDataType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
