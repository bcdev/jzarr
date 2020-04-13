package com.bc.zarr;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.blosc.Util;
import org.junit.*;
import ucar.ma2.DataType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class CompressorTest {

    @Test
    public void writeRead_NullCompressor() throws IOException {
        final Compressor compressor = CompressorFactory.nullCompressor;
        final byte[] input = {
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100
        };
        ByteArrayOutputStream os;
        ByteArrayInputStream is;

        final byte[] intermediate = Arrays.copyOf(input, input.length);

        //write
        is = new ByteArrayInputStream(input);
        os = new ByteArrayOutputStream();
        compressor.compress(is, os, DataType.BYTE, 0);
        assertThat(os.toByteArray(), is(equalTo(intermediate)));

        //read
        is = new ByteArrayInputStream(intermediate);
        os = new ByteArrayOutputStream();
        compressor.uncompress(is, os);
        assertThat(input, is(equalTo(os.toByteArray())));
    }

    @Test
    public void writeRead_ZipCompressor() throws IOException {
        final Compressor compressor = CompressorFactory.create("zlib", 1);
        final byte[] input = {
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100
        };
        ByteArrayOutputStream os;
        ByteArrayInputStream is;

        final byte[] intermediate = {120, 1, 75, 17, 75, 17, 19, 19, 75, 73, 73, 1, 98, 18, -104, 0, 127, -4, 13, -33};

        //write
        is = new ByteArrayInputStream(input);
        os = new ByteArrayOutputStream();
        compressor.compress(is, os, DataType.BYTE, 0);
        assertThat(os.toByteArray(), is(equalTo(intermediate)));

        //read
        is = new ByteArrayInputStream(intermediate);
        os = new ByteArrayOutputStream();
        compressor.uncompress(is, os);
        assertThat(input, is(equalTo(os.toByteArray())));
    }

    @Test
    public void writeRead_BloscCompressorBytes() throws IOException {
        final Compressor compressor = CompressorFactory.create("blosc", 2);
        final byte[] input = {
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100
        };
        ByteArrayOutputStream os;
        ByteArrayInputStream is;

        final byte[] intermediate = {
                2, 1, -109, 1, 55, 0, 0, 0, 55, 0, 0, 0, 71, 0, 0, 0,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100
        };

        //write
        is = new ByteArrayInputStream(input);
        os = new ByteArrayOutputStream();
        compressor.compress(is, os, DataType.BYTE, input.length);
        assertThat(os.toByteArray(), is(equalTo(intermediate)));

        //read
        is = new ByteArrayInputStream(os.toByteArray());
        os = new ByteArrayOutputStream();
        compressor.uncompress(is, os);
        assertThat(input, is(equalTo(os.toByteArray())));
    }

    @Test
    public void writeRead_BloscCompressorInts() throws IOException {
        final Compressor compressor = CompressorFactory.create("blosc", 5);
        final int[] input = {
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100
        };
        ByteArrayOutputStream os;
        ByteArrayInputStream is;

        final byte[] intermediate = {
                2, 1, -111, 4, -36, 0, 0, 0, -36, 0, 0, 0, 55, 0, 0, 0, 20, 0, 0, 0,
                31, 0, 0, 0, 40, -75, 47, -3, 32, -36, -75, 0, 0, 104, 0, 0,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                2, 0, -71, -104, 30, 68, 1, 44
        };

        ByteBuffer bb = ByteBuffer.allocate(input.length * 4);

        //write
        for (int i : input) {
            bb.putInt(i);
        }
        is = new ByteArrayInputStream(bb.array());
        os = new ByteArrayOutputStream();
        compressor.compress(is, os, DataType.INT, input.length);

        byte[] receivedIntermediate = os.toByteArray();
        assertThat(receivedIntermediate, is(equalTo(intermediate)));

        //read
        is = new ByteArrayInputStream(os.toByteArray());
        os = new ByteArrayOutputStream();
        compressor.uncompress(is, os);

        int[] received = new int[input.length];
        IntBuffer intBuffer = ByteBuffer.wrap(os.toByteArray()).asIntBuffer();
        intBuffer.get(received);
        assertThat(input, is(equalTo(received)));
    }
}