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