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

package com.bc.zarr;

import com.bc.zarr.storage.FileSystemStore;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ZarrArrayDataReaderWriterTest_2D_writeAndReadData {

    private String arrayName;
    private FileSystemStore store;

    @Before
    public void setUp() throws Exception {
        final FileSystem testFileSystem = Jimfs.newFileSystem(Configuration.windows());
        final Iterable<Path> rootDirectories = testFileSystem.getRootDirectories();
        final Path root = rootDirectories.iterator().next();
        store = new FileSystemStore(root);
        arrayName = "testPath";
//        Path testPath = root.resolve(arrayName);
//        Files.createDirectories(this.testPath);
    }

    @Test
    public void getNullCompressor() throws IOException {
        final int[] shape = {1, 1};
        final int[] chunkShape = {1, 1};
        final DataType dataType = DataType.i1; // Byte
        final Compressor compressor = CompressorFactory.create("null");
        final ArrayParams parameters = new ArrayParams()
                .shape(shape).chunks(chunkShape)
                .dataType(dataType)
                .compressor(compressor);
        final ZarrArray array = ZarrArray.create(
                new ZarrPath(arrayName), store, parameters, null);
        array.getCompressor();
        assertEquals(compressor, array.getCompressor());
    }

    @Test
    public void getBloscCompressor() throws IOException {
        final int[] shape = {1, 1};
        final int[] chunkShape = {1, 1};
        final DataType dataType = DataType.i1; // Byte
        final Compressor compressor = CompressorFactory.create("blosc");
        final ArrayParams parameters = new ArrayParams()
                .shape(shape).chunks(chunkShape)
                .dataType(dataType)
                .compressor(compressor);
        final ZarrArray array = ZarrArray.create(
                new ZarrPath(arrayName), store, parameters, null);
        assertEquals(compressor, array.getCompressor());
    }

    @Test
    public void getZlibCompressor() throws IOException {
        final int[] shape = {1, 1};
        final int[] chunkShape = {1, 1};
        final DataType dataType = DataType.i1; // Byte
        final Compressor compressor = CompressorFactory.create("zlib");
        final ArrayParams parameters = new ArrayParams()
                .shape(shape).chunks(chunkShape)
                .dataType(dataType)
                .compressor(compressor);
        final ZarrArray array = ZarrArray.create(
                new ZarrPath(arrayName), store, parameters, null);
        assertEquals(compressor, array.getCompressor());
    }

    @Test
    public void writeAndRead_Byte_Full() throws IOException, InvalidRangeException {
        //preparation
        final int width = 7;
        final int height = 5;
        final int[] shape = {height, width};  // common data model manner { y, x }
        final int chunkWidth = 4;
        final int chunkHeight = 3;
        final int[] chunkShape = {chunkHeight, chunkWidth};   // common data model manner { y, x }
        final DataType dataType = DataType.i1; // Byte
        final byte Fill = -5;
        final Compressor compressor = CompressorFactory.nullCompressor;
        final ArrayParams parameters = new ArrayParams()
                .shape(shape).chunks(chunkShape)
                .dataType(dataType).fillValue(Fill)
                .compressor(compressor);

        final ZarrArray array = ZarrArray.create(new ZarrPath(arrayName), store, parameters, null);

        final byte[] sourceBuffer = {
                0, 1, 2, 3, 4, 5, 6,
                7, 8, 9, 10, 11, 12, 13,
                14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27,
                28, 29, 30, 31, 32, 33, 34
        };
        array.write(sourceBuffer, shape, new int[]{0, 0});
        final byte[] targetBuffer = new byte[sourceBuffer.length];

        //execution
        array.read(targetBuffer, shape, new int[]{0, 0});

        //verification
        assertThat(targetBuffer, is(equalTo(sourceBuffer)));
    }

    @Test
    public void writeAndRead_Short_Full() throws IOException, InvalidRangeException {
        //preparation
        final int width = 7;
        final int height = 5;
        final int[] shape = {height, width};  // common data model manner { y, x }
        final int chunkWidth = 4;
        final int chunkHeight = 3;
        final int[] chunkShape = {chunkHeight, chunkWidth};   // common data model manner { y, x }
        final DataType dataType = DataType.i2; // Short
        final short Fill = -5;
        final Compressor compressor = CompressorFactory.nullCompressor;
        final ArrayParams parameters = new ArrayParams()
                .shape(shape).chunks(chunkShape)
                .dataType(dataType).fillValue(Fill)
                .compressor(compressor);

        final ZarrArray array = ZarrArray.create(new ZarrPath(arrayName), store, parameters, null);

        final short[] sourceBuffer = {
                0, 1, 2, 3, 4, 5, 6,
                7, 8, 9, 10, 11, 12, 13,
                14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27,
                28, 29, 30, 31, 32, 33, 34
        };
        array.write(sourceBuffer, shape, new int[]{0, 0});
        final short[] targetBuffer = new short[sourceBuffer.length];

        //execution
        array.read(targetBuffer, shape, new int[]{0, 0});

        //verification
        assertThat(targetBuffer, is(equalTo(sourceBuffer)));
    }

    @Test
    public void writeAndRead_Integer_Full() throws IOException, InvalidRangeException {
        //preparation
        final int width = 7;
        final int height = 5;
        final int[] shape = {height, width};  // common data model manner { y, x }
        final int chunkWidth = 4;
        final int chunkHeight = 3;
        final int[] chunkShape = {chunkHeight, chunkWidth};   // common data model manner { y, x }
        final DataType dataType = DataType.i4; // Integer
        final int Fill = -5;
        final Compressor compressor = CompressorFactory.nullCompressor;
        final ArrayParams parameters = new ArrayParams()
                .shape(shape).chunks(chunkShape)
                .dataType(dataType).fillValue(Fill)
                .compressor(compressor);
        final ZarrArray array = ZarrArray.create(new ZarrPath(arrayName), store, parameters, null);

        final int[] sourceBuffer = {
                0, 1, 2, 3, 4, 5, 6,
                7, 8, 9, 10, 11, 12, 13,
                14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27,
                28, 29, 30, 31, 32, 33, 34
        };
        array.write(sourceBuffer, shape, new int[]{0, 0});
        final int[] targetBuffer = new int[sourceBuffer.length];

        //execution
        array.read(targetBuffer, shape, new int[]{0, 0});

        //verification
        assertThat(targetBuffer, is(equalTo(sourceBuffer)));
    }

    @Test
    public void writeAndRead_Float_Full() throws IOException, InvalidRangeException {
        //preparation
        final int width = 7;
        final int height = 5;
        final int[] shape = {height, width};  // common data model manner { y, x }
        final int chunkWidth = 4;
        final int chunkHeight = 3;
        final int[] chunkShape = {chunkHeight, chunkWidth};   // common data model manner { y, x }
        final DataType dataType = DataType.f4; // Float
        final float Fill = -5;
        final Compressor compressor = CompressorFactory.nullCompressor;
        final ArrayParams parameters = new ArrayParams()
                .shape(shape).chunks(chunkShape)
                .dataType(dataType).fillValue(Fill)
                .compressor(compressor);
        final ZarrArray array = ZarrArray.create(new ZarrPath(arrayName), store, parameters, null);

        final float[] sourceBuffer = {
                0, 1, 2, 3, 4, 5, 6,
                7, 8, 9, 10, 11, 12, 13,
                14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27,
                28, 29, 30, 31, 32, 33, 34
        };
        array.write(sourceBuffer, shape, new int[]{0, 0});
        final float[] targetBuffer = new float[sourceBuffer.length];

        //execution
        array.read(targetBuffer, shape, new int[]{0, 0});

        //verification
        assertThat(targetBuffer, is(equalTo(sourceBuffer)));
    }

    @Test
    public void writeAndRead_Double_Full() throws IOException, InvalidRangeException {
        //preparation
        final int width = 7;
        final int height = 5;
        final int[] shape = {height, width};  // common data model manner { y, x }
        final int chunkWidth = 4;
        final int chunkHeight = 3;
        final int[] chunkShape = {chunkHeight, chunkWidth};   // common data model manner { y, x }
        final DataType dataType = DataType.f8; // Double
        final double Fill = -5;
        final Compressor compressor = CompressorFactory.nullCompressor;
        final ArrayParams parameters = new ArrayParams()
                .shape(shape).chunks(chunkShape)
                .dataType(dataType).fillValue(Fill)
                .compressor(compressor);

        final ZarrArray array = ZarrArray.create(new ZarrPath(arrayName), store, parameters, null);

        final double[] sourceBuffer = {
                0, 1, 2, 3, 4, 5, 6,
                7, 8, 9, 10, 11, 12, 13,
                14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27,
                28, 29, 30, 31, 32, 33, 34
        };
        array.write(sourceBuffer, shape, new int[]{0, 0});
        final double[] targetBuffer = new double[sourceBuffer.length];

        //execution
        array.read(targetBuffer, shape, new int[]{0, 0});

        //verification
        assertThat(targetBuffer, is(equalTo(sourceBuffer)));
    }
}