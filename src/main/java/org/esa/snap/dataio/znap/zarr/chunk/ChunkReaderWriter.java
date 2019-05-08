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
package org.esa.snap.dataio.znap.zarr.chunk;


import static org.esa.snap.dataio.znap.zarr.ZarrConstantsAndUtils.computeSize;

import org.esa.snap.dataio.znap.zarr.ZarrDataType;
import org.esa.snap.dataio.znap.zarr.ucar.NetCDF_Util;
import ucar.ma2.Array;
import ucar.ma2.DataType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

public abstract class ChunkReaderWriter {

    protected final Compressor compressor;
    protected final int[] chunkShape;
    protected final Number fill;
    private final int size;

    ChunkReaderWriter(Compressor compressor, int[] chunkShape, Number fill) {
        this.compressor = compressor;
        this.chunkShape = Arrays.copyOf(chunkShape, chunkShape.length);
        this.fill = fill;
        this.size = computeSize(chunkShape);
    }

    public static ChunkReaderWriter create(Compressor compressor, ZarrDataType dataType, int[] chunkShape, Number fill) {
        if (
                dataType == ZarrDataType.f8) {
            return new ChunkReaderWriterImpl_Double(compressor, chunkShape, fill);
        } else if (
                dataType == ZarrDataType.f4) {
            return new ChunkReaderWriterImpl_Float(compressor, chunkShape, fill);
        } else if (
                dataType == ZarrDataType.i4
                || dataType == ZarrDataType.u4) {
            return new ChunkReaderWriterImpl_Integer(compressor, chunkShape, fill);
        } else if (
                dataType == ZarrDataType.i2
                || dataType == ZarrDataType.u2) {
            return new ChunkReaderWriterImpl_Short(compressor, chunkShape, fill);
        } else if (
                dataType == ZarrDataType.i1
                || dataType == ZarrDataType.u1) {
            return new ChunkReaderWriterImpl_Byte(compressor, chunkShape, fill);
        } else {
            throw new IllegalStateException();
        }
    }

    public abstract Array read(Path path) throws IOException;

    public abstract void write(Path path, Array array) throws IOException;

    protected Array createFilled(final DataType dataType) {
        return NetCDF_Util.createFilledArray(dataType, chunkShape, fill);
    }

    protected int getSize() {
        return size;
    }
}
