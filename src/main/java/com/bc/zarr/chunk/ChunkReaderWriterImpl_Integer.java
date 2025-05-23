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

public class ChunkReaderWriterImpl_Integer extends ChunkReaderWriter {

    public ChunkReaderWriterImpl_Integer(ByteOrder order, Compressor compressor, int[] chunkShape, Number fill, Store store) {
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
                    final int[] ints = new int[getSize()];
                    try (
                            final ByteArrayInputStream bais = new ByteArrayInputStream(os.toByteArray());
                            final ImageInputStream iis = new MemoryCacheImageInputStream(bais)
                    ) {
                        iis.setByteOrder(order);
                        iis.readFully(ints, 0, ints.length);
                    }
                    return Array.factory(DataType.INT, chunkShape, ints);
                }
            } else {
                return createFilled(DataType.INT);
            }
        }
    }

    protected boolean isFillOnly(Array array) {
        if (fill == null) {
            return false;
        }
        final IndexIterator iter = array.getIndexIterator();
        while (iter.hasNext()) {
            if (iter.getIntNext() != fill.intValue()) {
                return false;
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
                final int[] ints = (int[]) array.get1DJavaArray(DataType.INT);
                iis.setByteOrder(order);
                iis.writeInts(ints, 0, ints.length);
                iis.seek(0);
                compressor.compress(is, os);
            }
        }
    }
}
