package com.bc.zarr;

import com.bc.zarr.chunk.*;
import com.bc.zarr.storage.FileSystemStore;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ZarrArrayTest_2D_writeAndEvaluateWrittenChunks {

    private Path testPath;
    private FileSystemStore store;
    private String arrayName;

    @Before
    public void setUp() throws Exception {
        final FileSystem testFileSystem = Jimfs.newFileSystem(Configuration.windows());
        final Iterable<Path> rootDirectories = testFileSystem.getRootDirectories();
        final Path root = rootDirectories.iterator().next();
        store = new FileSystemStore(root);
        arrayName = "testPath";
        testPath = root.resolve(arrayName);
//        Files.createDirectories(testPath);
    }

    @Test
    public void write_Byte_Full() throws IOException, InvalidRangeException {
        //preparation
        final int width = 7;
        final int height = 5;
        final int[] shape = {height, width};  // common data model manner { y, x }
        final int chunkWidth = 4;
        final int chunkHeight = 3;
        final int[] chunkShape = {chunkHeight, chunkWidth};   // common data model manner { y, x }
        final ZarrDataType dataType = ZarrDataType.i1; // Byte
        final byte Fill = -5;
        final Compressor compressor = CompressorFactory.nullCompressor;
        final ArrayParameters parameters = ArrayParameters.builder()
                .withShape(shape).withChunks(chunkShape)
                .withDataType(dataType).withFillValue(Fill)
                .withCompressor(compressor).build();

        final ZarrArray array = ZarrArray.create(new ZarrPath(arrayName), store, parameters, null);

        final byte[] sourceBuffer = {
                0, 1, 2, 3, 4, 5, 6,
                7, 8, 9, 10, 11, 12, 13,
                14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27,
                28, 29, 30, 31, 32, 33, 34
        };

        //execution
        array.write(sourceBuffer, new int[]{5, 7}, new int[]{0, 0});

        //verification
        final byte[][] expected = {
                {
                        0, 1, 2, 3,
                        7, 8, 9, 10,
                        14, 15, 16, 17
                }, {
                4, 5, 6, -5,
                11, 12, 13, -5,
                18, 19, 20, -5
        }, {
                21, 22, 23, 24,
                28, 29, 30, 31,
                -5, -5, -5, -5
        }, {
                25, 26, 27, -5,
                32, 33, 34, -5,
                -5, -5, -5, -5
        }
        };

        final String[] expectedNames = {"0.0", "0.1", "1.0", "1.1"};

        final List<Path> chunkFiles = getChunkFilesWritten();
        assertEquals(4, chunkFiles.size());

        final ChunkReaderWriter cr = new ChunkReaderWriterImpl_Byte(compressor, chunkShape, Fill, store);
        Array ma2Array;

        for (int i = 0; i < chunkFiles.size(); i++) {
            Path chunkFile = chunkFiles.get(i);
            assertEquals(testPath, chunkFile.getParent());
            assertEquals(12, Files.size(chunkFile));
            final String fileName = chunkFile.getFileName().toString();
            assertEquals(expectedNames[i], fileName);
            ma2Array = cr.read(arrayName + "/" + fileName);
            assertThat(ma2Array, is(notNullValue()));
            assertThat(ma2Array.get1DJavaArray(byte.class), is(equalTo(expected[i])));
        }
    }

    @Test
    public void write_Short_Full() throws IOException, InvalidRangeException {
        //preparation
        final int width = 7;
        final int height = 5;
        final int[] shape = {height, width};  // common data model manner { y, x }
        final int chunkWidth = 4;
        final int chunkHeight = 3;
        final int[] chunkShape = {chunkHeight, chunkWidth};   // common data model manner { y, x }
        final ZarrDataType dataType = ZarrDataType.i2; // Short
        final short Fill = -5;
        final Compressor compressor = CompressorFactory.nullCompressor;
        final ArrayParameters parameters = ArrayParameters.builder()
                .withShape(shape).withChunks(chunkShape)
                .withDataType(dataType).withFillValue(Fill)
                .withCompressor(compressor).build();
        final ZarrArray array = ZarrArray.create(new ZarrPath(arrayName), store, parameters, null);

        final short[] sourceBuffer = {
                0, 1, 2, 3, 4, 5, 6,
                7, 8, 9, 10, 11, 12, 13,
                14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27,
                28, 29, 30, 31, 32, 33, 34
        };

        //execution
        array.write(sourceBuffer, new int[]{5, 7}, new int[]{0, 0});

        //verification
        final short[][] expected = {
                {
                        0, 1, 2, 3,
                        7, 8, 9, 10,
                        14, 15, 16, 17
                }, {
                4, 5, 6, -5,
                11, 12, 13, -5,
                18, 19, 20, -5
        }, {
                21, 22, 23, 24,
                28, 29, 30, 31,
                -5, -5, -5, -5
        }, {
                25, 26, 27, -5,
                32, 33, 34, -5,
                -5, -5, -5, -5
        }
        };

        final String[] expectedNames = {"0.0", "0.1", "1.0", "1.1"};

        final List<Path> chunkFiles = getChunkFilesWritten();
        assertEquals(4, chunkFiles.size());

        final ChunkReaderWriter cr = new ChunkReaderWriterImpl_Short(ByteOrder.BIG_ENDIAN, compressor, chunkShape, Fill, store);
        Array ma2Array;

        for (int i = 0; i < chunkFiles.size(); i++) {
            Path chunkFile = chunkFiles.get(i);
            assertEquals(testPath, chunkFile.getParent());
            assertEquals(24, Files.size(chunkFile));
            final String fileName = chunkFile.getFileName().toString();
            assertEquals(expectedNames[i], fileName);
            ma2Array = cr.read(arrayName + "/" + fileName);
            assertThat(ma2Array, is(notNullValue()));
            assertThat(ma2Array.get1DJavaArray(short.class), is(equalTo(expected[i])));
        }
    }

    @Test
    public void write_Integer_Full() throws IOException, InvalidRangeException {
        //preparation
        final int width = 7;
        final int height = 5;
        final int[] shape = {height, width};  // common data model manner { y, x }
        final int chunkWidth = 4;
        final int chunkHeight = 3;
        final int[] chunkShape = {chunkHeight, chunkWidth};   // common data model manner { y, x }
        final ZarrDataType dataType = ZarrDataType.i4; // Integer
        final int Fill = -5;
        final Compressor compressor = CompressorFactory.nullCompressor;
        final ArrayParameters parameters = ArrayParameters.builder()
                .withShape(shape).withChunks(chunkShape)
                .withDataType(dataType).withFillValue(Fill)
                .withCompressor(compressor).build();

        final ZarrArray array = ZarrArray.create(new ZarrPath(arrayName), store, parameters, null);

        final int[] sourceBuffer = {
                0, 1, 2, 3, 4, 5, 6,
                7, 8, 9, 10, 11, 12, 13,
                14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27,
                28, 29, 30, 31, 32, 33, 34
        };

        //execution
        array.write(sourceBuffer, new int[]{5, 7}, new int[]{0, 0});

        //verification
        final int[][] expected = {
                {
                        0, 1, 2, 3,
                        7, 8, 9, 10,
                        14, 15, 16, 17
                }, {
                4, 5, 6, -5,
                11, 12, 13, -5,
                18, 19, 20, -5
        }, {
                21, 22, 23, 24,
                28, 29, 30, 31,
                -5, -5, -5, -5
        }, {
                25, 26, 27, -5,
                32, 33, 34, -5,
                -5, -5, -5, -5
        }
        };

        final String[] expectedNames = {"0.0", "0.1", "1.0", "1.1"};

        final List<Path> chunkFiles = getChunkFilesWritten();
        assertEquals(4, chunkFiles.size());

        final ChunkReaderWriter cr = new ChunkReaderWriterImpl_Integer(ByteOrder.BIG_ENDIAN, compressor, chunkShape, Fill, store);
        Array ma2Array;

        for (int i = 0; i < chunkFiles.size(); i++) {
            Path chunkFile = chunkFiles.get(i);
            assertEquals(testPath, chunkFile.getParent());
            assertEquals(48, Files.size(chunkFile));
            final String fileName = chunkFile.getFileName().toString();
            assertEquals(expectedNames[i], fileName);
            ma2Array = cr.read(arrayName + "/" + fileName);
            assertThat(ma2Array, is(notNullValue()));
            assertThat(ma2Array.get1DJavaArray(int.class), is(equalTo(expected[i])));
        }
    }

    @Test
    public void write_Float_Full() throws IOException, InvalidRangeException {
        //preparation
        final int width = 7;
        final int height = 5;
        final int[] shape = {height, width};  // common data model manner { y, x }
        final int chunkWidth = 4;
        final int chunkHeight = 3;
        final int[] chunkShape = {chunkHeight, chunkWidth};   // common data model manner { y, x }
        final ZarrDataType dataType = ZarrDataType.f4; // Float
        final float Fill = -5;
        final Compressor compressor = CompressorFactory.nullCompressor;
        final ArrayParameters parameters = ArrayParameters.builder()
                .withShape(shape).withChunks(chunkShape)
                .withDataType(dataType).withFillValue(Fill)
                .withCompressor(compressor).build();

        final ZarrArray array = ZarrArray.create(new ZarrPath(arrayName), store, parameters, null);

        final float[] sourceBuffer = {
                0, 1, 2, 3, 4, 5, 6,
                7, 8, 9, 10, 11, 12, 13,
                14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27,
                28, 29, 30, 31, 32, 33, 34
        };

        //execution
        array.write(sourceBuffer, new int[]{5, 7}, new int[]{0, 0});

        //verification
        final float[][] expected = {
                {
                        0, 1, 2, 3,
                        7, 8, 9, 10,
                        14, 15, 16, 17
                }, {
                4, 5, 6, -5,
                11, 12, 13, -5,
                18, 19, 20, -5
        }, {
                21, 22, 23, 24,
                28, 29, 30, 31,
                -5, -5, -5, -5
        }, {
                25, 26, 27, -5,
                32, 33, 34, -5,
                -5, -5, -5, -5
        }
        };

        final String[] expectedNames = {"0.0", "0.1", "1.0", "1.1"};

        final List<Path> chunkFiles = getChunkFilesWritten();
        assertEquals(4, chunkFiles.size());

        final ChunkReaderWriter cr = new ChunkReaderWriterImpl_Float(ByteOrder.BIG_ENDIAN, compressor, chunkShape, Fill, store);
        Array ma2Array;

        for (int i = 0; i < chunkFiles.size(); i++) {
            Path chunkFile = chunkFiles.get(i);
            assertEquals(testPath, chunkFile.getParent());
            assertEquals(48, Files.size(chunkFile));
            final String fileName = chunkFile.getFileName().toString();
            assertEquals(expectedNames[i], fileName);
            ma2Array = cr.read(arrayName + "/" + fileName);
            assertThat(ma2Array, is(notNullValue()));
            assertThat(ma2Array.get1DJavaArray(float.class), is(equalTo(expected[i])));
        }
    }

    @Test
    public void write_Double_Full() throws IOException, InvalidRangeException {
        //preparation
        final int width = 7;
        final int height = 5;
        final int[] shape = {height, width};  // common data model manner { y, x }
        final int chunkWidth = 4;
        final int chunkHeight = 3;
        final int[] chunkShape = {chunkHeight, chunkWidth};   // common data model manner { y, x }
        final ZarrDataType dataType = ZarrDataType.f8; // Double
        final double Fill = -5;
        final Compressor compressor = CompressorFactory.nullCompressor;
        final ArrayParameters parameters = ArrayParameters.builder()
                .withShape(shape).withChunks(chunkShape)
                .withDataType(dataType).withFillValue(Fill)
                .withCompressor(compressor).build();

        final ZarrArray array = ZarrArray.create(new ZarrPath(arrayName), store, parameters, null);

        final double[] sourceBuffer = {
                0, 1, 2, 3, 4, 5, 6,
                7, 8, 9, 10, 11, 12, 13,
                14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27,
                28, 29, 30, 31, 32, 33, 34
        };

        //execution
        array.write(sourceBuffer, new int[]{5, 7}, new int[]{0, 0});

        //verification
        final double[][] expected = {
                {
                        0, 1, 2, 3,
                        7, 8, 9, 10,
                        14, 15, 16, 17
                }, {
                4, 5, 6, -5,
                11, 12, 13, -5,
                18, 19, 20, -5
        }, {
                21, 22, 23, 24,
                28, 29, 30, 31,
                -5, -5, -5, -5
        }, {
                25, 26, 27, -5,
                32, 33, 34, -5,
                -5, -5, -5, -5
        }
        };

        final String[] expectedNames = {"0.0", "0.1", "1.0", "1.1"};

        final List<Path> chunkFiles = getChunkFilesWritten();
        assertEquals(4, chunkFiles.size());

        final ChunkReaderWriter cr = new ChunkReaderWriterImpl_Double(ByteOrder.BIG_ENDIAN, compressor, chunkShape, Fill, store);
        Array ma2Array;

        for (int i = 0; i < chunkFiles.size(); i++) {
            Path chunkFile = chunkFiles.get(i);
            assertEquals(testPath, chunkFile.getParent());
            assertEquals(96, Files.size(chunkFile));
            final String fileName = chunkFile.getFileName().toString();
            assertEquals(expectedNames[i], fileName);
            ma2Array = cr.read(arrayName + "/" + fileName);
            assertThat(ma2Array, is(notNullValue()));
            assertThat(ma2Array.get1DJavaArray(double.class), is(equalTo(expected[i])));
        }
    }

    public List<Path> getChunkFilesWritten() throws IOException {
        return Files.list(testPath).filter(path -> !path.getFileName().toString().startsWith(".")).collect(Collectors.toList());
    }
}