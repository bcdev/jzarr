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
package org.esa.snap.dataio.znap.zarr;

import org.esa.snap.dataio.znap.zarr.chunk.ChunkReaderWriter;
import org.esa.snap.dataio.znap.zarr.chunk.Compressor;
import org.esa.snap.dataio.znap.zarr.ucar.RawDataReader;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.nio.file.Path;

public class ZarrWriter {

    private final int[] _shape;
    private final int[] _chunks;
    private final ZarrDataType _dataType;
    private final Number _fillValue;
    private final Compressor _compressor;
    private final Path _dataPath;
    private final ChunkReaderWriter _chunkReaderWriter;

    public ZarrWriter(Path dataPath, int[] shape, int[] chunkShape, ZarrDataType dataType, Number fillValue, Compressor compressor) {
        _dataPath = dataPath;
        _shape = shape;
        _chunks = chunkShape;
        _dataType = dataType;
        _fillValue = fillValue;
        _compressor = compressor;
        _chunkReaderWriter = ChunkReaderWriter.create(compressor, dataType, chunkShape, fillValue);
    }

    public void write(Object sourceBuffer, int[] bufferShape, int[] to) throws IOException, InvalidRangeException {
        final int[][] chunkIndices = ZarrConstantsAndUtils.computeChunkIndices(_shape, _chunks, bufferShape, to);
        final Array netCdfBuffer = Array.factory(sourceBuffer).reshapeNoCopy(bufferShape);

        for (int[] chunkIndex : chunkIndices) {
            final String chunkFilename = ZarrConstantsAndUtils.createChunkFilename(chunkIndex);
            final Path chunkFilePath = _dataPath.resolve(chunkFilename);
            final int[] fromBufferPos = computeFrom(chunkIndex, to);
            final Array chunk = _chunkReaderWriter.read(chunkFilePath);
            final Array read = RawDataReader.read(chunk, fromBufferPos, netCdfBuffer);
            _chunkReaderWriter.write(chunkFilePath, read);
        }
    }

    private int[] computeFrom(int[] chunkIndex, int[] to) {
        int[] from = {0, 0};
        for (int i = 0; i < chunkIndex.length; i++) {
            int index = chunkIndex[i];
            from[i] = index * _chunks[i];
            from[i] -= to[i];
        }
        return from;
    }
}






