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
package com.bc.zarr.chunk;

import com.bc.zarr.Compressor;
import com.bc.zarr.storage.Store;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.IndexIterator;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.io.*;
import java.nio.ByteOrder;

public class ChunkReaderWriterImpl_Float extends ChunkReaderWriter {

    public ChunkReaderWriterImpl_Float(ByteOrder order, Compressor compressor, int[] chunkShape, Number fill, Store store) {
        super(order, compressor, chunkShape, fill, store);
    }

    @Override
    public Array read(String storeKey) throws IOException {
        try (
                final InputStream is = store.getInputStream(storeKey)
        ) {
            if (is != null) {
                try (
                        final ByteArrayOutputStream os = new ByteArrayOutputStream()
                ) {
                    compressor.uncompress(is, os);
                    final float[] floats = new float[getSize()];
                    try (
                            final ByteArrayInputStream bais = new ByteArrayInputStream(os.toByteArray());
                            final ImageInputStream iis = new MemoryCacheImageInputStream(bais)
                    ) {
                        iis.setByteOrder(order);
                        iis.readFully(floats, 0, floats.length);
                    }
                    return Array.factory(DataType.FLOAT, chunkShape, floats);
                }
            } else {
                return createFilled(DataType.FLOAT);
            }
        }
    }

    protected boolean isFillOnly(Array array) {
        if (fill == null) {
            return false;
        }
        final IndexIterator iter = array.getIndexIterator();
        final float fillValue = fill.floatValue();
        if (Float.isNaN(fillValue)) {
            while (iter.hasNext()) {
                if (! Float.isNaN(iter.getFloatNext())) {
                    return false;
                }
            }
        } else {
            while (iter.hasNext()) {
                if (iter.getFloatNext() != fillValue) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void write(String storeKey, Array array) throws IOException {
        if (isFillOnly(array)) {
            store.delete(storeKey);
        } else {
            try (
                    final ImageOutputStream iis = new MemoryCacheImageOutputStream(new ByteArrayOutputStream());
                    final InputStream is = new ZarrInputStreamAdapter(iis);
                    final OutputStream os = store.getOutputStream(storeKey)
            ) {
                final float[] floats = (float[]) array.get1DJavaArray(DataType.FLOAT);
                iis.setByteOrder(order);
                iis.writeFloats(floats, 0, floats.length);
                iis.seek(0);
                compressor.compress(is, os);
            }
        }
    }
}
