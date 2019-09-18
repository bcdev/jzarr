package com.bc.zarr;

import com.bc.zarr.chunk.ChunkReaderWriter;
import com.bc.zarr.storage.FileSystemStore;
import com.bc.zarr.storage.InMemoryStore;
import com.bc.zarr.storage.Store;
import com.bc.zarr.ucar.NetCDF_Util;
import com.bc.zarr.ucar.PartialDataCopier;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.io.*;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import static com.bc.zarr.CompressorFactory.nullCompressor;
import static com.bc.zarr.ZarrConstants.FILENAME_DOT_ZARRAY;

public class ZarrArray {

    private final int[] _shape;
    private final int[] _chunks;
    private final ZarrPath relativePath;
    private final ChunkReaderWriter _chunkReaderWriter;
    private final Map<Object, Object> _locks;
    private final DataType _dataType;
    private final Number _fillValue;
    private final Compressor _compressor;
    private final Store _store;
    private final ByteOrder _byteOrder;

    private ZarrArray(ZarrPath relativePath, int[] shape, int[] chunkShape, DataType dataType, ByteOrder order, Number fillValue, Compressor compressor, Store store) {
        this.relativePath = relativePath;
        _shape = shape;
        _chunks = chunkShape;
        _dataType = dataType;
        _fillValue = fillValue;
        if (compressor == null) {
            _compressor = nullCompressor;
        } else {
            _compressor = compressor;
        }
        _store = store;
        _chunkReaderWriter = ChunkReaderWriter.create(_compressor, _dataType, order, _chunks, _fillValue, _store);
        _locks = Collections.synchronizedMap(new TreeMap<>());
        _byteOrder = order;
    }

    public static ZarrArray open(String path) throws IOException {
        return open(Paths.get(path));
    }

    public static ZarrArray open(Path fileSystemPath) throws IOException {
        return open(new FileSystemStore(fileSystemPath));
    }

    public static ZarrArray open(Store store) throws IOException {
        return open(new ZarrPath(""), store);
    }

    public static ZarrArray open(ZarrPath relativePath, Store store) throws IOException {
        final ZarrPath zarrHeaderPath = relativePath.resolve(FILENAME_DOT_ZARRAY);
        try (
                final InputStream storageStream = store.getInputStream(zarrHeaderPath.storeKey);
                BufferedReader reader = new BufferedReader(new InputStreamReader(storageStream))
        ) {
            final ZarrHeader header = ZarrUtils.fromJson(reader, ZarrHeader.class);
            final int[] shape = header.getShape();
            final int[] chunks = header.getChunks();
            final DataType dataType = header.getRawDataType();
            final ByteOrder byteOrder = header.getByteOrder();
            final Number fillValue = header.getFill_value();
            final ZarrHeader.CompressorBean compressorBean = header.getCompressor();
            final Compressor compressor;
            if (compressorBean != null) {
                final String id = compressorBean.getId();
                final int level = compressorBean.getLevel();
                compressor = CompressorFactory.create(id, level);
            } else {
                compressor = nullCompressor;
            }
            return new ZarrArray(relativePath, shape, chunks, dataType, byteOrder, fillValue, compressor, store);
        }
    }

    public static ZarrArray create(ArrayParams arrayParams) throws IOException {
        return create(new InMemoryStore(), arrayParams);
    }

    public static ZarrArray create(ArrayParams arrayParams, Map<String, Object> attributes) throws IOException {
        return create(new InMemoryStore(), arrayParams, attributes);
    }

    public static ZarrArray create(String path, ArrayParams params) throws IOException {
        return create(path, params, null);
    }

    public static ZarrArray create(String path, ArrayParams params, Map<String, Object> attributes) throws IOException {
        final Path fsPath = Paths.get(path);
        return create(fsPath, params, attributes);
    }

    public static ZarrArray create(Path fsPath, ArrayParams params) throws IOException {
        return create(fsPath, params, null);
    }

    public static ZarrArray create(Path fsPath, ArrayParams params, Map<String, Object> attributes) throws IOException {
        final FileSystemStore store = new FileSystemStore(fsPath);
        return create(store, params, attributes);
    }

    public static ZarrArray create(Store store, ArrayParams params) throws IOException {
        return create(store, params, null);
    }

    public static ZarrArray create(Store store, ArrayParams params, Map<String, Object> attributes) throws IOException {
        return create(new ZarrPath(""), store, params, attributes);
    }

    public static ZarrArray create(ZarrPath relativePath, Store store, ArrayParams params) throws IOException {
        return create(relativePath, store, params, null);
    }

    public static ZarrArray create(ZarrPath relativePath, Store store, ArrayParams arrayParams, Map<String, Object> attributes) throws IOException {
        store.delete(relativePath.storeKey);
        final ArrayParams.Params params = arrayParams.build();
        final int[] shape = params.getShape();
        final int[] chunks = params.getChunks();
        final DataType dataType = params.getDataType();
        final Number fillValue = params.getFillValue();
        final Compressor compressor = params.getCompressor();
        final ByteOrder byteOrder = params.getByteOrder();
        final ZarrArray zarrArray = new ZarrArray(relativePath, shape, chunks, dataType, byteOrder, fillValue, compressor, store);
        zarrArray.writeZArrayHeader();
        zarrArray.writeAttributes(attributes);
        return zarrArray;
    }

    public DataType getDataType() {
        return _dataType;
    }

    public int[] getShape() {
        return Arrays.copyOf(_shape, _shape.length);
    }

    public int[] getChunks() {
        return Arrays.copyOf(_chunks, _chunks.length);
    }

    public Number getFillValue() {
        return _fillValue;
    }

    public ByteOrder getByteOrder() {
        return _byteOrder;
    }

    public void write(Number value) throws IOException, InvalidRangeException {
        final int[] shape = getShape();
        final int[] offset = new int[shape.length];
        write(value, shape, offset);
    }

    public void write(Number value, int[] shape, int[] offset) throws IOException, InvalidRangeException {
        final Object data = ZarrUtils.createDataBufferFilledWith(value, getDataType(), shape);
        write(data, shape, offset);
    }

    public void write(Object data, int[] dataShape, int[] offset) throws IOException, InvalidRangeException {
        final int[][] chunkIndices = ZarrUtils.computeChunkIndices(_shape, _chunks, dataShape, offset);
        final Array source = Array.factory(data).reshapeNoCopy(dataShape);

        for (int[] chunkIndex : chunkIndices) {
            final String chunkFilename = ZarrUtils.createChunkFilename(chunkIndex);
            final ZarrPath chunkFilePath = relativePath.resolve(chunkFilename);
            final int[] fromBufferPos = computeFrom(chunkIndex, offset, false);
            synchronized (_locks) {
                if (!_locks.containsKey(chunkFilename)) {
                    _locks.put(chunkFilename, chunkFilename);
                }
            }
            synchronized (_locks.get(chunkFilename)) {
                if (partialCopyingIsNotNeeded(dataShape, fromBufferPos)) {
                    _chunkReaderWriter.write(chunkFilePath.storeKey, source);
                } else {
                    final Array targetChunk = _chunkReaderWriter.read(chunkFilePath.storeKey);
                    PartialDataCopier.copy(fromBufferPos, source, targetChunk);
                    _chunkReaderWriter.write(chunkFilePath.storeKey, targetChunk);
                }
            }
        }
    }

    public Object read() throws IOException, InvalidRangeException {
        return read(getShape());
    }

    public Object read(int[] shape) throws IOException, InvalidRangeException {
        return read(shape, new int[shape.length]);
    }

    public Object read(int[] shape, int[] offset) throws IOException, InvalidRangeException {
        final Object data = ZarrUtils.createDataBuffer(getDataType(), shape);
        read(data, shape, offset);
        return data;
    }

    public void read(Object buffer, int[] bufferShape) throws IOException, InvalidRangeException {
        read(buffer, bufferShape, new int[bufferShape.length]);
    }

    public void read(Object buffer, int[] bufferShape, int[] offset) throws IOException, InvalidRangeException {
        if (!buffer.getClass().isArray()) {
            throw new IOException("Target buffer object is not an array.");
        }
        final int targetSize = java.lang.reflect.Array.getLength(buffer);
        final long expectedSize = ZarrUtils.computeSize(bufferShape);
        if (targetSize != expectedSize) {
            throw new IOException("Expected target buffer size is " + expectedSize + " but was " + targetSize);
        }
        final int[][] chunkIndices = ZarrUtils.computeChunkIndices(_shape, _chunks, bufferShape, offset);

        for (int[] chunkIndex : chunkIndices) {
            final String chunkFilename = ZarrUtils.createChunkFilename(chunkIndex);
            final ZarrPath chunkFilePath = relativePath.resolve(chunkFilename);
            final int[] fromChunkPos = computeFrom(chunkIndex, offset, true);
            final Array sourceChunk = _chunkReaderWriter.read(chunkFilePath.storeKey);
            if (partialCopyingIsNotNeeded(bufferShape, fromChunkPos)) {
                System.arraycopy(sourceChunk.getStorage(), 0, buffer, 0, (int) sourceChunk.getSize());
            } else {
                final Array target = NetCDF_Util.createArrayWithGivenStorage(buffer, bufferShape);
                PartialDataCopier.copy(fromChunkPos, sourceChunk, target);
            }
        }
    }

    private boolean partialCopyingIsNotNeeded(int[] bufferShape, int[] offset) {
        return isZeroOffset(offset) && isBufferShapeEqualChunkShape(bufferShape);
    }

    private boolean isBufferShapeEqualChunkShape(int[] bufferShape) {
        return Arrays.equals(bufferShape, _chunks);
    }

    private boolean isZeroOffset(int[] offset) {
        return Arrays.equals(offset, new int[offset.length]);
    }

    public void writeAttributes(Map<String, Object> attributes) throws IOException {
        ZarrUtils.writeAttributes(attributes, relativePath, _store);
    }

    public Map<String, Object> getAttributes() throws IOException {
        return ZarrUtils.readAttributes(relativePath, _store);
    }

    @Override
    public String toString() {
        return getClass().getCanonicalName() + "{" +
                "'/" + relativePath.storeKey + "' " +
                "shape=" + Arrays.toString(_shape) +
                ", chunks=" + Arrays.toString(_chunks) +
                ", dataType=" + _dataType +
                ", fillValue=" + _fillValue +
                ", compressor=" + _compressor.getId() + "/level=" + _compressor.getLevel() +
                ", store=" + _store.getClass().getSimpleName() +
                ", byteOrder=" + _byteOrder +
                '}';
    }

    private int[] computeFrom(int[] chunkIndex, int[] to, boolean read) {
        int[] from = new int[chunkIndex.length];
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

    private void writeZArrayHeader() throws IOException {
        final ZarrHeader zarrHeader = new ZarrHeader(_shape, _chunks, _dataType.toString(), _byteOrder, _fillValue, _compressor);
        final ZarrPath zArray = relativePath.resolve(FILENAME_DOT_ZARRAY);
        try (
                OutputStream os = _store.getOutputStream(zArray.storeKey);
                OutputStreamWriter writer = new OutputStreamWriter(os)
        ) {
            ZarrUtils.toJson(zarrHeader, writer, true);
        }
    }
}






