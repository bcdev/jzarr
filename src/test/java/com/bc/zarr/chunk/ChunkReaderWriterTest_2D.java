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

import static com.bc.zarr.ZarrUtils.computeSizeInteger;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertNotNull;

import com.bc.zarr.Compressor;
import com.bc.zarr.CompressorFactory;
import com.bc.zarr.TestUtils;
import com.bc.zarr.DataType;
import com.bc.zarr.storage.FileSystemStore;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.*;
import ucar.ma2.Array;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.Deflater;

public class ChunkReaderWriterTest_2D {

    private String chunkStoreKey;
    private FileSystemStore store;
    private Path jimfsChunkPath;

    @Before
    public void setUp() throws Exception {
        final Path root = Jimfs.newFileSystem(Configuration.windows()).getRootDirectories().iterator().next();
        final Path testRootPath = root.resolve("test");
        Files.createDirectories(testRootPath);
        chunkStoreKey = "0.0";
        jimfsChunkPath = testRootPath.resolve(chunkStoreKey);
        store = new FileSystemStore(testRootPath);
    }

    @Test
    public void createChunkReaderWriter_compresserIsNullValue() throws NoSuchFieldException, IllegalAccessException {
        //execution
        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(null, DataType.i1, ByteOrder.BIG_ENDIAN, new int[]{3, 4}, 5, store);

        final Object compressor = TestUtils.getPrivateFieldObject(readerWriter, "compressor");
        assertThat(compressor, is(sameInstance(CompressorFactory.nullCompressor)));
    }

    @Test
    public void read_Bytes_NullCompressor_ChunkFileDoesNotExist() throws IOException {
        final int[] shape = {2, 3};
        final Number fill = 6;
        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(CompressorFactory.nullCompressor, DataType.i1, ByteOrder.BIG_ENDIAN, shape, fill, store);

        //execution
        final Array array = readerWriter.read(chunkStoreKey);

        assertNotNull(array);
        assertThat(array.getShape(), is(equalTo(shape)));
        final byte[] expectedValues = new byte[6];
        Arrays.fill(expectedValues, fill.byteValue());
        assertThat(expectedValues, is(equalTo(array.get1DJavaArray(ucar.ma2.DataType.BYTE))));
    }

    @Test
    public void read_Bytes_ZipCompressor_ChunkFileDoesNotExist() throws IOException {
        final int[] shape = {2, 3};
        final Number fill = 7;
        final Compressor compressor = CompressorFactory.create("zlib", "level", 1);
        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, DataType.i1, ByteOrder.BIG_ENDIAN, shape, fill, store);

        //execution
        final Array array = readerWriter.read(chunkStoreKey);

        assertNotNull(array);
        assertThat(array.getShape(), is(equalTo(shape)));
        final byte[] expectedValues = new byte[computeSizeInteger(shape)];
        Arrays.fill(expectedValues, fill.byteValue());
        assertThat(expectedValues, is(equalTo(array.get1DJavaArray(ucar.ma2.DataType.BYTE))));
    }

    @Test
    public void read_Short_ZipCompressor_ChunkFileDoesNotExist() throws IOException {
        final int[] shape = {2, 3};
        final Number fill = 8;
        final Compressor compressor = CompressorFactory.create("zlib", "level", 1);
        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, DataType.i2, ByteOrder.BIG_ENDIAN, shape, fill, store);

        //execution
        final Array array = readerWriter.read(chunkStoreKey);

        assertNotNull(array);
        assertThat(array.getShape(), is(equalTo(shape)));
        final short[] expectedValues = new short[computeSizeInteger(shape)];
        Arrays.fill(expectedValues, fill.shortValue());
        assertThat(expectedValues, is(equalTo(array.get1DJavaArray(ucar.ma2.DataType.SHORT))));
    }

    @Test
    public void read_Integer_ZipCompressor_ChunkFileDoesNotExist() throws IOException {
        final int[] shape = {2, 3};
        final Number fill = 8;
        final Compressor compressor = CompressorFactory.create("zlib", "level", 1);
        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, DataType.i4, ByteOrder.BIG_ENDIAN, shape, fill, store);

        //execution
        final Array array = readerWriter.read(chunkStoreKey);

        assertNotNull(array);
        assertThat(array.getShape(), is(equalTo(shape)));
        final int[] expectedValues = new int[computeSizeInteger(shape)];
        Arrays.fill(expectedValues, fill.intValue());
        assertThat(expectedValues, is(equalTo(array.get1DJavaArray(ucar.ma2.DataType.INT))));
    }

    @Test
    public void read_Float_ZipCompressor_ChunkFileDoesNotExist() throws IOException {
        final int[] shape = {2, 3};
        final Number fill = 8;
        final Compressor compressor = CompressorFactory.create("zlib", "level", 1);
        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, DataType.f4, ByteOrder.BIG_ENDIAN, shape, fill, store);

        //execution
        final Array array = readerWriter.read(chunkStoreKey);

        assertNotNull(array);
        assertThat(array.getShape(), is(equalTo(shape)));
        final float[] expectedValues = new float[computeSizeInteger(shape)];
        Arrays.fill(expectedValues, fill.floatValue());
        assertThat(expectedValues, is(equalTo(array.get1DJavaArray(ucar.ma2.DataType.FLOAT))));
    }

    @Test
    public void read_Double_ZipCompressor_ChunkFileDoesNotExist() throws IOException {
        final int[] shape = {2, 3};
        final Number fill = 8;
        final Compressor compressor = CompressorFactory.create("zlib", "level", 1);
        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, DataType.f8, ByteOrder.BIG_ENDIAN, shape, fill, store);

        //execution
        final Array array = readerWriter.read(chunkStoreKey);

        assertNotNull(array);
        assertThat(array.getShape(), is(equalTo(shape)));
        final double[] expectedValues = new double[computeSizeInteger(shape)];
        Arrays.fill(expectedValues, fill.doubleValue());
        assertThat(expectedValues, is(equalTo(array.get1DJavaArray(ucar.ma2.DataType.DOUBLE))));
    }

    @Test
    public void read_Bytes_NullCompressor_ChunkFileExist() throws IOException {
        final Compressor compressor = CompressorFactory.nullCompressor;
        final byte[] bytes = {1, 2, 3, 4, 5, 6};

        final OutputStream outputStream = Files.newOutputStream(jimfsChunkPath);
        outputStream.write(bytes);
        outputStream.close();
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, DataType.i1, ByteOrder.BIG_ENDIAN, shape, 3, store);

        //execution
        final Array array = readerWriter.read(chunkStoreKey);

        assertNotNull(array);
        assertThat(array.getShape(), is(equalTo(shape)));
        assertThat(array.get1DJavaArray(ucar.ma2.DataType.BYTE), is(equalTo(bytes)));
    }

    @Test
    public void write_Bytes_NullCompressor_ChunkFileExist() throws IOException {
        final Compressor compressor = CompressorFactory.nullCompressor;
        final byte[] bytes = {1, 2, 3, 4, 5, 6};
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, DataType.i1, ByteOrder.BIG_ENDIAN, shape, 3, store);

        //execution
        readerWriter.write(chunkStoreKey, Array.factory(ucar.ma2.DataType.BYTE, shape, bytes));

        final InputStream inputStream = Files.newInputStream(jimfsChunkPath);
        final byte[] buffer = new byte[100];
        final int size = inputStream.read(buffer);
        assertThat(size, equalTo(6));
        final byte[] written = Arrays.copyOf(buffer, size);
        assertThat(written, is(equalTo(bytes)));
    }

    @Test
    public void read_Bytes_ZipCompressor_ChunkFileExist() throws IOException {
        final int level = 1;
        final Compressor compressor = CompressorFactory.create("zlib", "level", level);
        final byte[] bytes = {1, 2, 3, 4, 5, 6};
        final int[] shape = {2, 3};

        try (final OutputStream outputStream = Files.newOutputStream(jimfsChunkPath)) {
            final Deflater compresser = new Deflater(level);
            compresser.setInput(bytes);
            compresser.finish();
            final byte[] buffer = new byte[100];
            final int length = compresser.deflate(buffer);
            outputStream.write(buffer, 0, length);
        }

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, DataType.i1, ByteOrder.BIG_ENDIAN, shape, 3, store);

        //execution
        final Array array = readerWriter.read(chunkStoreKey);

        assertNotNull(array);
        assertThat(array.getShape(), is(equalTo(shape)));
        assertThat(bytes, is(equalTo(array.get1DJavaArray(ucar.ma2.DataType.BYTE))));
    }

    @Test
    public void writeRead_Bytes_NullCompressor() throws IOException {
        final Compressor compressor = CompressorFactory.nullCompressor;
        final byte[] bytes = {1, 2, 3, 4, 5, 6};
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, DataType.i1, ByteOrder.BIG_ENDIAN, shape, 3, store);

        //execution write
        readerWriter.write(chunkStoreKey, Array.factory(ucar.ma2.DataType.BYTE, shape, bytes));

        //intermediate verification
        assertThat(Files.size(jimfsChunkPath), is(equalTo(6L)));

        //execution read
        final Array read = readerWriter.read(chunkStoreKey);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(bytes, is(equalTo(read.get1DJavaArray(ucar.ma2.DataType.BYTE))));
    }

    @Test
    public void writeRead_Bytes_ZipCompressor() throws IOException {
        final Compressor compressor = CompressorFactory.create("zlib", "level", 1);
        final byte[] bytes = {1, 2, 3, 4, 5, 6};
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, DataType.i1, ByteOrder.BIG_ENDIAN, shape, 3, store);

        //execution write
        readerWriter.write(chunkStoreKey, Array.factory(ucar.ma2.DataType.BYTE, shape, bytes));

        //intermediate verification
        assertThat(Files.size(jimfsChunkPath), is(equalTo(14L)));

        //execution read
        final Array read = readerWriter.read(chunkStoreKey);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(bytes, is(equalTo(read.get1DJavaArray(ucar.ma2.DataType.BYTE))));
    }

    @Test
    public void writeRead_Short_NullCompressor() throws IOException {
        final Compressor compressor = CompressorFactory.nullCompressor;
        final short[] input = {
                1, 2, 3,
                -1, -2, -3
        };
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, DataType.i2, ByteOrder.BIG_ENDIAN, shape, 3, store);

        //execution write
        readerWriter.write(chunkStoreKey, Array.factory(ucar.ma2.DataType.SHORT, shape, input));

        //intermediate verification
        assertThat(Files.size(jimfsChunkPath), is(equalTo(12L)));

        //execution read
        final Array read = readerWriter.read(chunkStoreKey);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(input, is(equalTo(read.get1DJavaArray(ucar.ma2.DataType.SHORT))));
    }

    @Test
    public void writeRead_Short_ZipCompressor() throws IOException {
        final Compressor compressor = CompressorFactory.create("zlib", "level", 1);
        final short[] shorts = {1, 2, 3, 4, 5, 6};
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, DataType.i2, ByteOrder.BIG_ENDIAN, shape, 3, store);

        //execution write
        readerWriter.write(chunkStoreKey, Array.factory(ucar.ma2.DataType.SHORT, shape, shorts));

        //intermediate verification
        assertThat(Files.size(jimfsChunkPath), is(equalTo(20L)));

        //execution read
        final Array read = readerWriter.read(chunkStoreKey);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(shorts, is(equalTo(read.get1DJavaArray(ucar.ma2.DataType.SHORT))));
    }

    @Test
    public void writeRead_Integer_NullCompressor() throws IOException {
        final Compressor compressor = CompressorFactory.nullCompressor;
        final int[] input = {
                1, 2, 3,
                -1, -2, -3
        };
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, DataType.i4, ByteOrder.BIG_ENDIAN, shape, 3, store);

        //execution write
        readerWriter.write(chunkStoreKey, Array.factory(ucar.ma2.DataType.INT, shape, input));

        //intermediate verification
        assertThat(Files.size(jimfsChunkPath), is(equalTo(24L)));

        //execution read
        final Array read = readerWriter.read(chunkStoreKey);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(input, is(equalTo(read.get1DJavaArray(ucar.ma2.DataType.INT))));
    }

    @Test
    public void writeRead_Integer_ZipCompressor() throws IOException {
        final Compressor compressor = CompressorFactory.create("zlib", "level", 1);
        final int[] ints = {1, 2, 3, 4, 5, 6};
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, DataType.i4, ByteOrder.BIG_ENDIAN, shape, 3, store);

        //execution write
        readerWriter.write(chunkStoreKey, Array.factory(ucar.ma2.DataType.INT, shape, ints));

        //intermediate verification
        assertThat(Files.size(jimfsChunkPath), is(equalTo(26L)));

        //execution read
        final Array read = readerWriter.read(chunkStoreKey);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(ints, is(equalTo(read.get1DJavaArray(ucar.ma2.DataType.INT))));
    }

    @Test
    public void writeRead_Float_NullCompressor() throws IOException {
        final Compressor compressor = CompressorFactory.nullCompressor;
        final float[] input = {
                1, 2, 3,
                -1, -2, -3
        };
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, DataType.f4, ByteOrder.BIG_ENDIAN, shape, 3, store);

        //execution write
        readerWriter.write(chunkStoreKey, Array.factory(ucar.ma2.DataType.FLOAT, shape, input));

        //intermediate verification
        assertThat(Files.size(jimfsChunkPath), is(equalTo(24L)));

        //execution read
        final Array read = readerWriter.read(chunkStoreKey);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(input, is(equalTo(read.get1DJavaArray(ucar.ma2.DataType.FLOAT))));
    }

    @Test
    public void writeRead_Float_ZipCompressor() throws IOException {
        final Compressor compressor = CompressorFactory.create("zlib", "level", 1);
        final float[] floats = {1, 2, 3, 4, 5, 6};
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, DataType.f4, ByteOrder.BIG_ENDIAN, shape, 3, store);

        //execution write
        readerWriter.write(chunkStoreKey, Array.factory(ucar.ma2.DataType.FLOAT, shape, floats));

        //intermediate verification
        assertThat(Files.size(jimfsChunkPath), is(equalTo(25L)));

        //execution read
        final Array read = readerWriter.read(chunkStoreKey);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(floats, is(equalTo(read.get1DJavaArray(ucar.ma2.DataType.FLOAT))));
    }

    @Test
    public void writeRead_Double_NullCompressor() throws IOException {
        final Compressor compressor = CompressorFactory.nullCompressor;
        final double[] input = {
                1, 2, 3,
                -1, -2, -3
        };
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, DataType.f8, ByteOrder.BIG_ENDIAN, shape, 3, store);

        //execution write
        readerWriter.write(chunkStoreKey, Array.factory(ucar.ma2.DataType.DOUBLE, shape, input));

        //intermediate verification
        assertThat(Files.size(jimfsChunkPath), is(equalTo(48L)));

        //execution read
        final Array read = readerWriter.read(chunkStoreKey);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(input, is(equalTo(read.get1DJavaArray(ucar.ma2.DataType.DOUBLE))));
    }

    @Test
    public void writeRead_Double_ZipCompressor() throws IOException {
        final Compressor compressor = CompressorFactory.create("zlib", "level", 1);
        final double[] doubles = {1, 2, 3, 4, 5, 6};
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, DataType.f8, ByteOrder.BIG_ENDIAN, shape, 3, store);

        //execution write
        readerWriter.write(chunkStoreKey, Array.factory(ucar.ma2.DataType.DOUBLE, shape, doubles));

        //intermediate verification
        assertThat(Files.size(jimfsChunkPath), is(equalTo(28L)));

        //execution read
        final Array read = readerWriter.read(chunkStoreKey);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(doubles, is(equalTo(read.get1DJavaArray(ucar.ma2.DataType.DOUBLE))));
    }

    @Test
    public void writeRead_Float_Fill_ZipCompressor() throws IOException {
        final Compressor compressor = CompressorFactory.create("zlib", "level", 1);
        final float[] floats = {3, 3, 3, 3, 3, 3};
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, DataType.f4, ByteOrder.BIG_ENDIAN, shape, 3, store);
        Files.deleteIfExists(jimfsChunkPath);

        //execution write
        readerWriter.write(chunkStoreKey, Array.factory(ucar.ma2.DataType.FLOAT, shape, floats));

        //intermediate verification
        assertThat(Files.exists(jimfsChunkPath), is(equalTo(false)));

        //execution read
        final Array read = readerWriter.read(chunkStoreKey);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(floats, is(equalTo(read.get1DJavaArray(ucar.ma2.DataType.FLOAT))));
    }
}