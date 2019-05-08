package org.esa.snap.dataio.znap.zarr.chunk;

import static org.esa.snap.dataio.znap.zarr.ZarrConstantsAndUtils.computeSize;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.esa.snap.dataio.znap.zarr.ZarrDataType;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.*;
import ucar.ma2.Array;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.Deflater;

public class ChunkReaderWriterTest_2D {

    private Path chunkPath;

    @Before
    public void setUp() throws Exception {
        final Path root = Jimfs.newFileSystem(Configuration.windows()).getRootDirectories().iterator().next();
        final Path testPath = root.resolve("test");
        Files.createDirectories(testPath);
        chunkPath = testPath.resolve("0.0");
    }

    @Test
    public void read_Bytes_NullCompressor_ChunkFileDoesNotExist() throws IOException {
        final int[] shape = {2, 3};
        final Number fill = 6;
        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(Compressor.Null, ZarrDataType.i1, shape, fill);

        //execution
        final Array array = readerWriter.read(chunkPath);

        assertNotNull(array);
        assertThat(array.getShape(), is(equalTo(shape)));
        final byte[] expectedValues = new byte[6];
        Arrays.fill(expectedValues, fill.byteValue());
        assertThat(expectedValues, is(equalTo(array.get1DJavaArray(Byte.class))));
    }

    @Test
    public void read_Bytes_ZipCompressor_ChunkFileDoesNotExist() throws IOException {
        final int[] shape = {2, 3};
        final Number fill = 7;
        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(Compressor.Zip_L1, ZarrDataType.i1, shape, fill);

        //execution
        final Array array = readerWriter.read(chunkPath);

        assertNotNull(array);
        assertThat(array.getShape(), is(equalTo(shape)));
        final byte[] expectedValues = new byte[computeSize(shape)];
        Arrays.fill(expectedValues, fill.byteValue());
        assertThat(expectedValues, is(equalTo(array.get1DJavaArray(Byte.class))));
    }

    @Test
    public void read_Short_ZipCompressor_ChunkFileDoesNotExist() throws IOException {
        final int[] shape = {2, 3};
        final Number fill = 8;
        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(Compressor.Zip_L1, ZarrDataType.i2, shape, fill);

        //execution
        final Array array = readerWriter.read(chunkPath);

        assertNotNull(array);
        assertThat(array.getShape(), is(equalTo(shape)));
        final short[] expectedValues = new short[computeSize(shape)];
        Arrays.fill(expectedValues, fill.shortValue());
        assertThat(expectedValues, is(equalTo(array.get1DJavaArray(Short.class))));
    }

    @Test
    public void read_Integer_ZipCompressor_ChunkFileDoesNotExist() throws IOException {
        final int[] shape = {2, 3};
        final Number fill = 8;
        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(Compressor.Zip_L1, ZarrDataType.i4, shape, fill);

        //execution
        final Array array = readerWriter.read(chunkPath);

        assertNotNull(array);
        assertThat(array.getShape(), is(equalTo(shape)));
        final int[] expectedValues = new int[computeSize(shape)];
        Arrays.fill(expectedValues, fill.intValue());
        assertThat(expectedValues, is(equalTo(array.get1DJavaArray(Integer.class))));
    }

    @Test
    public void read_Float_ZipCompressor_ChunkFileDoesNotExist() throws IOException {
        final int[] shape = {2, 3};
        final Number fill = 8;
        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(Compressor.Zip_L1, ZarrDataType.f4, shape, fill);

        //execution
        final Array array = readerWriter.read(chunkPath);

        assertNotNull(array);
        assertThat(array.getShape(), is(equalTo(shape)));
        final float[] expectedValues = new float[computeSize(shape)];
        Arrays.fill(expectedValues, fill.floatValue());
        assertThat(expectedValues, is(equalTo(array.get1DJavaArray(Float.class))));
    }

    @Test
    public void read_Double_ZipCompressor_ChunkFileDoesNotExist() throws IOException {
        final int[] shape = {2, 3};
        final Number fill = 8;
        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(Compressor.Zip_L1, ZarrDataType.f8, shape, fill);

        //execution
        final Array array = readerWriter.read(chunkPath);

        assertNotNull(array);
        assertThat(array.getShape(), is(equalTo(shape)));
        final double[] expectedValues = new double[computeSize(shape)];
        Arrays.fill(expectedValues, fill.doubleValue());
        assertThat(expectedValues, is(equalTo(array.get1DJavaArray(Double.class))));
    }

    @Test
    public void read_Bytes_NullCompressor_ChunkFileExist() throws IOException {
        final Compressor compressor = Compressor.Null;
        final byte[] bytes = {1, 2, 3, 4, 5, 6};

        final OutputStream outputStream = Files.newOutputStream(chunkPath);
        outputStream.write(bytes);
        outputStream.close();
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, ZarrDataType.i1, shape, 3);

        //execution
        final Array array = readerWriter.read(chunkPath);

        assertNotNull(array);
        assertThat(array.getShape(), is(equalTo(shape)));
        assertThat(array.get1DJavaArray(Byte.class), is(equalTo(bytes)));
    }

    @Test
    public void write_Bytes_NullCompressor_ChunkFileExist() throws IOException {
        final Compressor compressor = Compressor.Null;
        final byte[] bytes = {1, 2, 3, 4, 5, 6};
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, ZarrDataType.i1, shape, 3);

        //execution
        readerWriter.write(chunkPath, Array.factory(bytes).reshape(shape));

        final InputStream inputStream = Files.newInputStream(chunkPath);
        final byte[] buffer = new byte[100];
        final int size = inputStream.read(buffer);
        assertThat(size, equalTo(6));
        final byte[] written = Arrays.copyOf(buffer, size);
        assertThat(written, is(equalTo(bytes)));
    }

    @Test
    public void read_Bytes_ZipCompressor_ChunkFileExist() throws IOException {
        final Compressor compressor = Compressor.Zip_L1;
        final byte[] bytes = {1, 2, 3, 4, 5, 6};
        final int[] shape = {2, 3};

        try (final OutputStream outputStream = Files.newOutputStream(chunkPath)) {
            final Deflater compresser = new Deflater(compressor.getLevel());
            compresser.setInput(bytes);
            compresser.finish();
            final byte[] buffer = new byte[100];
            final int length = compresser.deflate(buffer);
            outputStream.write(buffer, 0, length);
        }

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, ZarrDataType.i1, shape, 3);

        //execution
        final Array array = readerWriter.read(chunkPath);

        assertNotNull(array);
        assertThat(array.getShape(), is(equalTo(shape)));
        assertThat(bytes, is(equalTo(array.get1DJavaArray(Byte.class))));
    }

    @Test
    public void writeRead_Bytes_NullCompressor() throws IOException {
        final Compressor compressor = Compressor.Null;
        final byte[] bytes = {1, 2, 3, 4, 5, 6};
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, ZarrDataType.i1, shape, 3);

        //execution write
        readerWriter.write(chunkPath, Array.factory(bytes).reshape(shape));

        //intermediate verification
        assertThat(Files.size(chunkPath), is(equalTo(6L)));

        //execution read
        final Array read = readerWriter.read(chunkPath);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(bytes, is(equalTo(read.get1DJavaArray(Byte.class))));
    }

    @Test
    public void writeRead_Bytes_ZipCompressor() throws IOException {
        final Compressor compressor = Compressor.Zip_L1;
        final byte[] bytes = {1, 2, 3, 4, 5, 6};
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, ZarrDataType.i1, shape, 3);

        //execution write
        readerWriter.write(chunkPath, Array.factory(bytes).reshape(shape));

        //intermediate verification
        assertThat(Files.size(chunkPath), is(equalTo(14L)));

        //execution read
        final Array read = readerWriter.read(chunkPath);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(bytes, is(equalTo(read.get1DJavaArray(Byte.class))));
    }

    @Test
    public void writeRead_Short_NullCompressor() throws IOException {
        final Compressor compressor = Compressor.Null;
        final short[] input = {
                1, 2, 3,
                -1, -2, -3
        };
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, ZarrDataType.i2, shape, 3);

        //execution write
        readerWriter.write(chunkPath, Array.factory(input).reshape(shape));

        //intermediate verification
        assertThat(Files.size(chunkPath), is(equalTo(12L)));

        //execution read
        final Array read = readerWriter.read(chunkPath);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(input, is(equalTo(read.get1DJavaArray(Short.class))));
    }

    @Test
    public void writeRead_Short_ZipCompressor() throws IOException {
        final Compressor compressor = Compressor.Zip_L1;
        final short[] shorts = {1, 2, 3, 4, 5, 6};
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, ZarrDataType.i2, shape, 3);

        //execution write
        readerWriter.write(chunkPath, Array.factory(shorts).reshape(shape));

        //intermediate verification
        assertThat(Files.size(chunkPath), is(equalTo(20L)));

        //execution read
        final Array read = readerWriter.read(chunkPath);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(shorts, is(equalTo(read.get1DJavaArray(Short.class))));
    }

    @Test
    public void writeRead_Integer_NullCompressor() throws IOException {
        final Compressor compressor = Compressor.Null;
        final int[] input = {
                1, 2, 3,
                -1, -2, -3
        };
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, ZarrDataType.i4, shape, 3);

        //execution write
        readerWriter.write(chunkPath, Array.factory(input).reshape(shape));

        //intermediate verification
        assertThat(Files.size(chunkPath), is(equalTo(24L)));

        //execution read
        final Array read = readerWriter.read(chunkPath);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(input, is(equalTo(read.get1DJavaArray(Integer.class))));
    }

    @Test
    public void writeRead_Integer_ZipCompressor() throws IOException {
        final Compressor compressor = Compressor.Zip_L1;
        final int[] ints = {1, 2, 3, 4, 5, 6};
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, ZarrDataType.i4, shape, 3);

        //execution write
        readerWriter.write(chunkPath, Array.factory(ints).reshape(shape));

        //intermediate verification
        assertThat(Files.size(chunkPath), is(equalTo(26L)));

        //execution read
        final Array read = readerWriter.read(chunkPath);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(ints, is(equalTo(read.get1DJavaArray(Integer.class))));
    }

    @Test
    public void writeRead_Float_NullCompressor() throws IOException {
        final Compressor compressor = Compressor.Null;
        final float[] input = {
                1, 2, 3,
                -1, -2, -3
        };
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, ZarrDataType.f4, shape, 3);

        //execution write
        readerWriter.write(chunkPath, Array.factory(input).reshape(shape));

        //intermediate verification
        assertThat(Files.size(chunkPath), is(equalTo(24L)));

        //execution read
        final Array read = readerWriter.read(chunkPath);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(input, is(equalTo(read.get1DJavaArray(Float.class))));
    }

    @Test
    public void writeRead_Float_ZipCompressor() throws IOException {
        final Compressor compressor = Compressor.Zip_L1;
        final float[] floats = {1, 2, 3, 4, 5, 6};
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, ZarrDataType.f4, shape, 3);

        //execution write
        readerWriter.write(chunkPath, Array.factory(floats).reshape(shape));

        //intermediate verification
        assertThat(Files.size(chunkPath), is(equalTo(25L)));

        //execution read
        final Array read = readerWriter.read(chunkPath);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(floats, is(equalTo(read.get1DJavaArray(Float.class))));
    }

    @Test
    public void writeRead_Double_NullCompressor() throws IOException {
        final Compressor compressor = Compressor.Null;
        final double[] input = {
                1, 2, 3,
                -1, -2, -3
        };
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, ZarrDataType.f8, shape, 3);

        //execution write
        readerWriter.write(chunkPath, Array.factory(input).reshape(shape));

        //intermediate verification
        assertThat(Files.size(chunkPath), is(equalTo(48L)));

        //execution read
        final Array read = readerWriter.read(chunkPath);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(input, is(equalTo(read.get1DJavaArray(Double.class))));
    }

    @Test
    public void writeRead_Double_ZipCompressor() throws IOException {
        final Compressor compressor = Compressor.Zip_L1;
        final double[] doubles = {1, 2, 3, 4, 5, 6};
        final int[] shape = {2, 3};

        final ChunkReaderWriter readerWriter = ChunkReaderWriter.create(compressor, ZarrDataType.f8, shape, 3);

        //execution write
        readerWriter.write(chunkPath, Array.factory(doubles).reshape(shape));

        //intermediate verification
        assertThat(Files.size(chunkPath), is(equalTo(28L)));

        //execution read
        final Array read = readerWriter.read(chunkPath);

        //verification
        assertThat(read, is(notNullValue()));
        assertThat(read.getShape(), is(equalTo(shape)));
        assertThat(doubles, is(equalTo(read.get1DJavaArray(Double.class))));
    }
}