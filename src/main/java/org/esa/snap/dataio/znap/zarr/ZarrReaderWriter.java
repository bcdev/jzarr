package org.esa.snap.dataio.znap.zarr;

import org.esa.snap.dataio.znap.zarr.chunk.ChunkReaderWriter;
import org.esa.snap.dataio.znap.zarr.chunk.Compressor;
import org.esa.snap.dataio.znap.zarr.ucar.NetCDF_Util;
import org.esa.snap.dataio.znap.zarr.ucar.Partial2dDataCopier;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class ZarrReaderWriter implements ZarrWriter, ZarrReader {

    private final int[] _shape;
    private final int[] _chunks;
    private final Path _dataPath;
    private final ChunkReaderWriter _chunkReaderWriter;
    private final Map<Object, Object> _locks;

    public ZarrReaderWriter(Path dataPath, int[] shape, int[] chunkShape, ZarrDataType dataType, Number fillValue, Compressor compressor) {
        _dataPath = dataPath;
        _shape = shape;
        _chunks = chunkShape;
        _chunkReaderWriter = ChunkReaderWriter.create(compressor, dataType, chunkShape, fillValue);
        _locks = Collections.synchronizedMap(new TreeMap<>());
    }

    @Override
    public void write(Object dataBuffer, int[] bufferShape, int[] to) throws IOException, InvalidRangeException {
        final int[][] chunkIndices = ZarrConstantsAndUtils.computeChunkIndices(_shape, _chunks, bufferShape, to);
        final Array source = Array.factory(dataBuffer).reshapeNoCopy(bufferShape);

        for (int[] chunkIndex : chunkIndices) {
            final String chunkFilename = ZarrConstantsAndUtils.createChunkFilename(chunkIndex);
            synchronized (_locks) {
                if (!_locks.containsKey(chunkFilename)) {
                    _locks.put(chunkFilename, chunkFilename);
                }
            }
            final Path chunkFilePath = _dataPath.resolve(chunkFilename);
            final int[] fromBufferPos = computeFrom(chunkIndex, to, false);
            synchronized (_locks.get(chunkFilename)) {
                final Array targetChunk = _chunkReaderWriter.read(chunkFilePath);
                final Array read = Partial2dDataCopier.copy(fromBufferPos, source, targetChunk);
                _chunkReaderWriter.write(chunkFilePath, read);
            }
        }
    }

    @Override
    public void read(Object targetBuffer, int[] bufferShape, int[] from) throws IOException, InvalidRangeException {
        final int[][] chunkIndices = ZarrConstantsAndUtils.computeChunkIndices(_shape, _chunks, bufferShape, from);
        final Array target = NetCDF_Util.createArrayWithGivenStorage(targetBuffer, bufferShape);

        for (int[] chunkIndex : chunkIndices) {
            final String chunkFilename = ZarrConstantsAndUtils.createChunkFilename(chunkIndex);
            final Path chunkFilePath = _dataPath.resolve(chunkFilename);
            final int[] fromChunkPos = computeFrom(chunkIndex, from, true);
            final Array sourceChunk = _chunkReaderWriter.read(chunkFilePath);
            Partial2dDataCopier.copy(fromChunkPos, sourceChunk, target);
        }

    }

    private int[] computeFrom(int[] chunkIndex, int[] to, boolean read) {
        int[] from = {0, 0};
        for (int i = 0; i < chunkIndex.length; i++) {
            int index = chunkIndex[i];
            from[i] = index * _chunks[i];
            from[i] -= to[i];
        }
        if (read) {
            for (int i1 = 0; i1 < from.length; i1++) {
                from[i1] *= -1;
            }
        }
        return from;
    }
}






