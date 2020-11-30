/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.bc.zarr.chunk;

import com.bc.zarr.Compressor;
import com.bc.zarr.storage.Store;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.io.*;
import java.nio.ByteOrder;

public class ChunkReaderWriterImpl_Double extends ChunkReaderWriter {

    public ChunkReaderWriterImpl_Double(ByteOrder order, Compressor compressor, int[] chunkShape, Number fill, Store store) {
        super(order, compressor, chunkShape, fill, store);
    }

    @Override
    public Array read(String storeKey) throws IOException {
        try (
                InputStream is = store.getInputStream(storeKey)
        ) {
            if (is != null) {
                try (
                        final ByteArrayOutputStream os = new ByteArrayOutputStream()
                ) {
                    compressor.uncompress(is, os);
                    final double[] doubles = new double[getSize()];
                    try (
                            final ByteArrayInputStream bais = new ByteArrayInputStream(os.toByteArray());
                            final ImageInputStream iis = new MemoryCacheImageInputStream(bais)
                    ) {
                        iis.setByteOrder(order);
                        iis.readFully(doubles, 0, doubles.length);
                    }
                    return Array.factory(DataType.DOUBLE, chunkShape, doubles);
                }
            } else {
                return createFilled(DataType.DOUBLE);
            }
        }
    }

    @Override
    public void write(String storeKey, Array array) throws IOException {
        try (
                final ImageOutputStream iis = new MemoryCacheImageOutputStream(new ByteArrayOutputStream());
                final InputStream is = new ZarrInputStreamAdapter(iis);
                final OutputStream os = store.getOutputStream(storeKey)
        ) {
            final double[] doubles = (double[]) array.get1DJavaArray(DataType.DOUBLE);
            iis.setByteOrder(order);
            iis.writeDoubles(doubles, 0, doubles.length);
            iis.seek(0);
            compressor.compress(is, os);
        }
    }
}
