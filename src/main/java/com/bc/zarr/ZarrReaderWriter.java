package com.bc.zarr;

import com.bc.zarr.chunk.ChunkReaderWriter;
import com.bc.zarr.chunk.Compressor;
import com.bc.zarr.ucar.NetCDF_Util;
import com.bc.zarr.ucar.Partial2dDataCopier;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class ZarrReaderWriter implements ZarrWriter, ZarrReader {

    private final int[] _shape;
    private final int[] _chunks;
    private final Path _dataPath;
    private final ChunkReaderWriter _chunkReaderWriter;
    private final Map<Object, Object> _locks;
    private final ZarrDataType _dataType;

    public ZarrReaderWriter(Path dataPath, int[] shape, int[] chunkShape, ZarrDataType dataType, Number fillValue, Compressor compressor) {
        _dataPath = dataPath;
        _shape = shape;
        _chunks = chunkShape;
        _dataType = dataType;
        _chunkReaderWriter = ChunkReaderWriter.create(compressor, dataType, chunkShape, fillValue);
        _locks = Collections.synchronizedMap(new TreeMap<>());
    }

    @Override
    public ZarrDataType getDataType() {
        return _dataType;
    }

    @Override
    public int[] getShape() {
        return Arrays.copyOf(_shape, _shape.length);
    }

    @Override
    public int[] getChunks() {
        return Arrays.copyOf(_chunks, _chunks.length);
    }

    @Override
    public void write(Object dataBuffer, int[] bufferShape, int[] to) throws IOException, InvalidRangeException {
        final int[][] chunkIndices = ZarrUtils.computeChunkIndices(_shape, _chunks, bufferShape, to);
        final Array source = Array.factory(dataBuffer).reshapeNoCopy(bufferShape);

        for (int[] chunkIndex : chunkIndices) {
            final String chunkFilename = ZarrUtils.createChunkFilename(chunkIndex);
            final Path chunkFilePath = _dataPath.resolve(chunkFilename);
            final int[] fromBufferPos = computeFrom(chunkIndex, to, false);
            if (partialCopyingIsNotNeeded(bufferShape, fromBufferPos)) {
                _chunkReaderWriter.write(chunkFilePath, source);
            } else {
                synchronized (_locks) {
                    if (!_locks.containsKey(chunkFilename)) {
                        _locks.put(chunkFilename, chunkFilename);
                    }
                }
                synchronized (_locks.get(chunkFilename)) {
                    final Array targetChunk = _chunkReaderWriter.read(chunkFilePath);
                    final Array read = Partial2dDataCopier.copy(fromBufferPos, source, targetChunk);
                    _chunkReaderWriter.write(chunkFilePath, read);
                }
            }
        }
    }

    @Override
    public void read(Object targetBuffer, int[] bufferShape, int[] from) throws IOException, InvalidRangeException {
        final int[][] chunkIndices = ZarrUtils.computeChunkIndices(_shape, _chunks, bufferShape, from);

        for (int[] chunkIndex : chunkIndices) {
            final String chunkFilename = ZarrUtils.createChunkFilename(chunkIndex);
            final Path chunkFilePath = _dataPath.resolve(chunkFilename);
            final int[] fromChunkPos = computeFrom(chunkIndex, from, true);
            final Array sourceChunk = _chunkReaderWriter.read(chunkFilePath);
            if (partialCopyingIsNotNeeded(bufferShape, fromChunkPos)) {
                System.arraycopy(sourceChunk.getStorage(), 0, targetBuffer, 0, (int) sourceChunk.getSize());
            } else {
                final Array target = NetCDF_Util.createArrayWithGivenStorage(targetBuffer, bufferShape);
                Partial2dDataCopier.copy(fromChunkPos, sourceChunk, target);
            }
        }
    }

    public boolean partialCopyingIsNotNeeded(int[] bufferShape, int[] position) {
        return isZeroOffset(position) && isBufferShapeEqualChunkShape(bufferShape);
    }

    public boolean isBufferShapeEqualChunkShape(int[] bufferShape) {
        return Arrays.equals(bufferShape, _chunks);
    }

    public boolean isZeroOffset(int[] position) {
        return Arrays.equals(position, new int[position.length]);
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






