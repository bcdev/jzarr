package com.bc.zarr;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.bc.zarr.chunk.Compressor;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ZarrReaderWriterTest_2D_writeAndReadData {

    private Path testPath;

    @Before
    public void setUp() throws Exception {
        final FileSystem testFileSystem = Jimfs.newFileSystem(Configuration.windows());
        final Iterable<Path> rootDirectories = testFileSystem.getRootDirectories();
        final Path root = rootDirectories.iterator().next();
        testPath = root.resolve("testPath");
        Files.createDirectories(testPath);
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
        final ZarrDataType dataType = ZarrDataType.i1; // Byte
        final byte Fill = -5;
        final Compressor compressor = Compressor.Null;
        final ZarrReaderWriter zarrReaderWriter = new ZarrReaderWriter(testPath, shape, chunkShape, dataType, Fill, compressor);

        final byte[] sourceBuffer = {
                0, 1, 2, 3, 4, 5, 6,
                7, 8, 9, 10, 11, 12, 13,
                14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27,
                28, 29, 30, 31, 32, 33, 34
        };
        zarrReaderWriter.write(sourceBuffer, shape, new int[]{0, 0});
        final byte[] targetBuffer = new byte[sourceBuffer.length];

        //execution
        zarrReaderWriter.read(targetBuffer, shape, new int[]{0, 0});

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
        final ZarrDataType dataType = ZarrDataType.i2; // Short
        final short Fill = -5;
        final Compressor compressor = Compressor.Null;
        final ZarrReaderWriter zarrReaderWriter = new ZarrReaderWriter(testPath, shape, chunkShape, dataType, Fill, compressor);

        final short[] sourceBuffer = {
                0, 1, 2, 3, 4, 5, 6,
                7, 8, 9, 10, 11, 12, 13,
                14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27,
                28, 29, 30, 31, 32, 33, 34
        };
        zarrReaderWriter.write(sourceBuffer, shape, new int[]{0, 0});
        final short[] targetBuffer = new short[sourceBuffer.length];

        //execution
        zarrReaderWriter.read(targetBuffer, shape, new int[]{0, 0});

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
        final ZarrDataType dataType = ZarrDataType.i4; // Integer
        final int Fill = -5;
        final Compressor compressor = Compressor.Null;
        final ZarrReaderWriter zarrReaderWriter = new ZarrReaderWriter(testPath, shape, chunkShape, dataType, Fill, compressor);

        final int[] sourceBuffer = {
                0, 1, 2, 3, 4, 5, 6,
                7, 8, 9, 10, 11, 12, 13,
                14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27,
                28, 29, 30, 31, 32, 33, 34
        };
        zarrReaderWriter.write(sourceBuffer, shape, new int[]{0, 0});
        final int[] targetBuffer = new int[sourceBuffer.length];

        //execution
        zarrReaderWriter.read(targetBuffer, shape, new int[]{0, 0});

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
        final ZarrDataType dataType = ZarrDataType.f4; // Float
        final float Fill = -5;
        final Compressor compressor = Compressor.Null;
        final ZarrReaderWriter zarrReaderWriter = new ZarrReaderWriter(testPath, shape, chunkShape, dataType, Fill, compressor);

        final float[] sourceBuffer = {
                0, 1, 2, 3, 4, 5, 6,
                7, 8, 9, 10, 11, 12, 13,
                14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27,
                28, 29, 30, 31, 32, 33, 34
        };
        zarrReaderWriter.write(sourceBuffer, shape, new int[]{0, 0});
        final float[] targetBuffer = new float[sourceBuffer.length];

        //execution
        zarrReaderWriter.read(targetBuffer, shape, new int[]{0, 0});

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
        final ZarrDataType dataType = ZarrDataType.f8; // Double
        final double Fill = -5;
        final Compressor compressor = Compressor.Null;
        final ZarrReaderWriter zarrReaderWriter = new ZarrReaderWriter(testPath, shape, chunkShape, dataType, Fill, compressor);

        final double[] sourceBuffer = {
                0, 1, 2, 3, 4, 5, 6,
                7, 8, 9, 10, 11, 12, 13,
                14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27,
                28, 29, 30, 31, 32, 33, 34
        };
        zarrReaderWriter.write(sourceBuffer, shape, new int[]{0, 0});
        final double[] targetBuffer = new double[sourceBuffer.length];

        //execution
        zarrReaderWriter.read(targetBuffer, shape, new int[]{0, 0});

        //verification
        assertThat(targetBuffer, is(equalTo(sourceBuffer)));
    }
}