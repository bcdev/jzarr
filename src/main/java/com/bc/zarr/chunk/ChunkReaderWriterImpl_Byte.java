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
package com.bc.zarr.chunk;

import com.bc.zarr.Compressor;
import com.bc.zarr.storage.Store;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.io.*;

public class ChunkReaderWriterImpl_Byte extends ChunkReaderWriter {

    public ChunkReaderWriterImpl_Byte(Compressor compressor, int[] chunkShape, Number fill, Store store) {
        super(null, compressor, chunkShape, fill, store);
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
                    final byte[] b = os.toByteArray();
                    return Array.factory(DataType.BYTE, chunkShape, b);
                }
            } else {
                return createFilled(DataType.BYTE);
            }
        }
    }

    @Override
    public void write(String storeKey, Array array) throws IOException {
        final byte[] bytes = (byte[]) array.get1DJavaArray(DataType.BYTE);
        try (
                final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
                final OutputStream os = store.getOutputStream(storeKey)
        ) {
            compressor.compress(is, os);
        }
    }
}
