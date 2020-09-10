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
import com.bc.zarr.CompressorFactory;
import com.bc.zarr.DataType;
import com.bc.zarr.storage.Store;
import com.bc.zarr.ucar.NetCDF_Util;
import ucar.ma2.Array;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;

import static com.bc.zarr.ZarrUtils.computeSizeInteger;

public abstract class ChunkReaderWriter {

    protected final Compressor compressor;
    final int[] chunkShape;
    protected final Number fill;
    protected final Store store;
    protected final ByteOrder order;
    private final int size;

    ChunkReaderWriter(ByteOrder order, Compressor compressor, int[] chunkShape, Number fill, Store store) {
        if (compressor != null) {
            this.compressor = compressor;
        } else {
            this.compressor = CompressorFactory.nullCompressor;
        }
        this.chunkShape = Arrays.copyOf(chunkShape, chunkShape.length);
        this.fill = fill;
        this.size = computeSizeInteger(chunkShape);
        this.store = store;
        this.order = order;
    }

    public static ChunkReaderWriter create(Compressor compressor, DataType dataType, ByteOrder order, int[] chunkShape, Number fill, Store store) {
        if (dataType == DataType.f8) {
            return new ChunkReaderWriterImpl_Double(order, compressor, chunkShape, fill, store);
        } else if (dataType == DataType.f4) {
            return new ChunkReaderWriterImpl_Float(order, compressor, chunkShape, fill, store);
        } else if (dataType == DataType.i8) {
            return new ChunkReaderWriterImpl_Long(order, compressor, chunkShape, fill, store);
        } else if (dataType == DataType.i4 || dataType == DataType.u4) {
            return new ChunkReaderWriterImpl_Integer(order, compressor, chunkShape, fill, store);
        } else if (dataType == DataType.i2 || dataType == DataType.u2) {
            return new ChunkReaderWriterImpl_Short(order, compressor, chunkShape, fill, store);
        } else if (dataType == DataType.i1 || dataType == DataType.u1) {
            return new ChunkReaderWriterImpl_Byte(compressor, chunkShape, fill, store);
        } else {
            throw new IllegalStateException();
        }
    }

    public abstract Array read(String path) throws IOException;

    public abstract void write(String path, Array array) throws IOException;

    protected Array createFilled(final ucar.ma2.DataType dataType) {
        return NetCDF_Util.createFilledArray(dataType, chunkShape, fill);
    }

    protected int getSize() {
        return this.size;
    }

}
