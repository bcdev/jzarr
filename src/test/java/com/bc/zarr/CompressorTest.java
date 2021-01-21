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

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.bc.zarr.chunk.ZarrInputStreamAdapter;
import org.junit.*;

import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteOrder;
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
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final MemoryCacheImageOutputStream iis = new MemoryCacheImageOutputStream(baos);
        iis.write(input);

        ByteArrayOutputStream os;
        ByteArrayInputStream is;

        final byte[] intermediate = Arrays.copyOf(input, input.length);

        //write
        is = new ByteArrayInputStream(input);
        os = new ByteArrayOutputStream();
        compressor.compress(is, os);
        assertThat(os.toByteArray(), is(equalTo(intermediate)));

        //read
        is = new ByteArrayInputStream(intermediate);
        os = new ByteArrayOutputStream();
        compressor.uncompress(is, os);
        assertThat(input, is(equalTo(os.toByteArray())));
    }

    @Test
    public void writeRead_ZipCompressor() throws IOException {
        final Compressor compressor = CompressorFactory.create("zlib");
        final ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
        final int[] input = {
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100
        };
        final MemoryCacheImageOutputStream iis = new MemoryCacheImageOutputStream(new ByteArrayOutputStream());
        iis.setByteOrder(byteOrder);
        iis.writeInts(input, 0, input.length);
        iis.seek(0);

        ByteArrayOutputStream os;
        ByteArrayInputStream is;

        final byte[] intermediate = {120, 1, 99, 96, 96, 72, 97, 96, 96, 16, 3, 98, 24, 13, 98, -61, -8, 32, 49, -104, 56, 58, 27, -90, 110, -48, -86, 5, 0, -42, 101, 13, -33};

        //write
        os = new ByteArrayOutputStream();
        compressor.compress(new ZarrInputStreamAdapter(iis), os);
        final byte[] compressed = os.toByteArray();
        assertThat(compressed, is(equalTo(intermediate)));

        //read
        is = new ByteArrayInputStream(compressed);
        os = new ByteArrayOutputStream();
        compressor.uncompress(is, os);
        final ByteArrayInputStream bais = new ByteArrayInputStream(os.toByteArray());
        final MemoryCacheImageInputStream resultIis = new MemoryCacheImageInputStream(bais);
        resultIis.setByteOrder(byteOrder);
        final int[] uncompressed = new int[input.length];
        resultIis.readFully(uncompressed, 0, uncompressed.length);
        assertThat(input, is(equalTo(uncompressed)));
    }

    @Test
    public void writeRead_BloscCompressor() throws IOException {
        final Compressor compressor = CompressorFactory.create("blosc");
        final ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
        final int[] input = {
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100
        };
        final MemoryCacheImageOutputStream iis = new MemoryCacheImageOutputStream(new ByteArrayOutputStream());
        iis.setByteOrder(byteOrder);
        iis.writeInts(input, 0, input.length);
        iis.seek(0);

        ByteArrayOutputStream os;
        ByteArrayInputStream is;

        final byte[] intermediate = {2, 1, 33, 1, -36, 0, 0, 0, -36, 0, 0, 0, 73, 0, 0, 0, 20, 0, 0, 0, 49, 0, 0, 0, -5, 17, 0, 0, 0, 100, 0, 0, 0, 22, 0, 0, 0, 100, 0, 0, 0, 22, 0, 0, 0, 22, 0, 0, 0, 22, 0, 0, 0, 100, 0, 0, 0, 100, 32, 0, 0, 20, 0, 15, 44, 0, -111, 80, 22, 0, 0, 0, 100};

        //write
        os = new ByteArrayOutputStream();
        compressor.compress(new ZarrInputStreamAdapter(iis), os);
        final byte[] compressed = os.toByteArray();
        assertThat(compressed, is(equalTo(intermediate)));

        //read
        is = new ByteArrayInputStream(compressed);
        os = new ByteArrayOutputStream();
        compressor.uncompress(is, os);
        final ByteArrayInputStream bais = new ByteArrayInputStream(os.toByteArray());
        final MemoryCacheImageInputStream resultIis = new MemoryCacheImageInputStream(bais);
        resultIis.setByteOrder(byteOrder);
        final int[] uncompressed = new int[input.length];
        resultIis.readFully(uncompressed, 0, uncompressed.length);
        assertThat(input, is(equalTo(uncompressed)));
    }

    @Test
    public void read_BloscCompressor_DefaultAvailable() throws IOException {
        final Compressor compressor = CompressorFactory.create("blosc");
        final ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
        final int[] input = {
            100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
            100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
            100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
            100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
            100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100
        };
        final MemoryCacheImageOutputStream iis = new MemoryCacheImageOutputStream(new ByteArrayOutputStream());
        iis.setByteOrder(byteOrder);
        iis.writeInts(input, 0, input.length);
        iis.seek(0);

        ByteArrayOutputStream os;
        InputStream is;

        final byte[] intermediate = {2, 1, 33, 1, -36, 0, 0, 0, -36, 0, 0, 0, 73, 0, 0, 0, 20, 0, 0, 0, 49, 0, 0, 0, -5, 17, 0, 0, 0, 100, 0, 0, 0, 22, 0, 0, 0, 100, 0, 0, 0, 22, 0, 0, 0, 22, 0, 0, 0, 22, 0, 0, 0, 100, 0, 0, 0, 100, 32, 0, 0, 20, 0, 15, 44, 0, -111, 80, 22, 0, 0, 0, 100};

        //write
        os = new ByteArrayOutputStream();
        compressor.compress(new ZarrInputStreamAdapter(iis), os);
        final byte[] compressed = os.toByteArray();
        assertThat(compressed, is(equalTo(intermediate)));

        //read
        is = new MockAWSChecksumValidatingInputStream(new ByteArrayInputStream(compressed));
        os = new ByteArrayOutputStream();
        compressor.uncompress(is, os);
        final ByteArrayInputStream bais = new ByteArrayInputStream(os.toByteArray());
        final MemoryCacheImageInputStream resultIis = new MemoryCacheImageInputStream(bais);
        resultIis.setByteOrder(byteOrder);
        final int[] uncompressed = new int[input.length];
        resultIis.readFully(uncompressed, 0, uncompressed.length);
        assertThat(input, is(equalTo(uncompressed)));
    }

    // Simulates a software.amazon.awssdk.services.s3.checksums.ChecksumValidatingInputStream which
    // does not provide it's own implementation of available() and always returns 0
    private static class MockAWSChecksumValidatingInputStream extends InputStream {

        private final InputStream in;

        public MockAWSChecksumValidatingInputStream(InputStream in) {
            this.in = in;
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }

        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            return in.read(buf, off, len);
        }

        @Override
        public synchronized void reset() throws IOException {
            in.reset();
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }
}