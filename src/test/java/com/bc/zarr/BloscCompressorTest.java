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

import com.bc.zarr.chunk.ZarrInputStreamAdapter;
import org.blosc.JBlosc;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class BloscCompressorTest {

    @BeforeClass
    public static void beforeClass() {

        final String bloscJnaLibraryPath = System.getProperty("bloscJnaLibraryPath");
        System.err.println(bloscJnaLibraryPath);
        final boolean bloscPathDefined = bloscJnaLibraryPath != null;
        final boolean bloscAvailable = isBloscAvailable();
        if (bloscPathDefined && !bloscAvailable) {
            Assert.fail("Property for blosc has been configured, but blosc is not available.");
        } else {
            Assume.assumeTrue("Blosc library path is not configured. Blosc specifc tests are not executed.", bloscAvailable); // test is skipped if blosc is not available
        }
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
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
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

        //write
        os = new ByteArrayOutputStream();
        compressor.compress(new ZarrInputStreamAdapter(iis), os);
        final byte[] compressed = os.toByteArray();

        //read
        is = new MockAWSChecksumValidatingInputStream(new ByteArrayInputStream(compressed), len -> len > 16 ? 7 : len);
        os = new ByteArrayOutputStream();
        compressor.uncompress(is, os);
        final ByteArrayInputStream bais = new ByteArrayInputStream(os.toByteArray());
        final MemoryCacheImageInputStream resultIis = new MemoryCacheImageInputStream(bais);
        resultIis.setByteOrder(byteOrder);
        final int[] uncompressed = new int[input.length];
        resultIis.readFully(uncompressed, 0, uncompressed.length);
        assertThat(uncompressed, is(equalTo(input)));
    }

    @Test
    public void read_BloscCompressor_ReducedBytesRead() throws IOException {
        final Compressor compressor = CompressorFactory.create("blosc");
        final ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
        final int[] input = {
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
                100, 22, 100, 22, 22, 22, 100, 100, 100, 22, 100,
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

        //write
        os = new ByteArrayOutputStream();
        compressor.compress(new ZarrInputStreamAdapter(iis), os);
        final byte[] compressed = os.toByteArray();

        //read
        is = new MockAWSChecksumValidatingInputStream(new ByteArrayInputStream(compressed), len -> len > 1 ? len - 1 : len);
        os = new ByteArrayOutputStream();
        compressor.uncompress(is, os);
        final ByteArrayInputStream bais = new ByteArrayInputStream(os.toByteArray());
        final MemoryCacheImageInputStream resultIis = new MemoryCacheImageInputStream(bais);
        resultIis.setByteOrder(byteOrder);
        final int[] uncompressed = new int[input.length];
        resultIis.readFully(uncompressed, 0, uncompressed.length);
        assertThat(uncompressed, is(equalTo(input)));
    }

    @Test
    public void testThatAllSupportedBloscCasesWorksWithAWS() throws IOException {
        final int[] blocksizes = {0, 200, 2000};
        for (String cname : CompressorFactory.BloscCompressor.supportedCnames) {
            for (int cLevel = 0; cLevel < 10; cLevel++) {
                for (int blocksize : blocksizes) {
                    for (int shuffle = 0; shuffle < 3; shuffle++) {
                        final Map<String, Object> bloscProperties = new LinkedHashMap<>();
                        bloscProperties.put(CompressorFactory.BloscCompressor.keyCname, cname);
                        bloscProperties.put(CompressorFactory.BloscCompressor.keyClevel, cLevel);
                        bloscProperties.put(CompressorFactory.BloscCompressor.keyBlocksize, blocksize);
                        bloscProperties.put(CompressorFactory.BloscCompressor.keyShuffle, shuffle);
//                        System.out.println("bloscProperties = " + bloscProperties.toString());
                        final Compressor compressor = CompressorFactory.create("blosc", bloscProperties);

                        final ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
                        final int[] input = new int[4321];
                        for (int i = 0; i < input.length; i++) {
                            input[i] = i;
                        }
                        final MemoryCacheImageOutputStream iis = new MemoryCacheImageOutputStream(new ByteArrayOutputStream());
                        iis.setByteOrder(byteOrder);
                        iis.writeInts(input, 0, input.length);
                        iis.seek(0);

                        ByteArrayOutputStream os;
                        InputStream is;

                        //write
                        os = new ByteArrayOutputStream();
                        compressor.compress(new ZarrInputStreamAdapter(iis), os);
                        final byte[] compressed = os.toByteArray();

                        //read
                        is = new MockAWSChecksumValidatingInputStream(new ByteArrayInputStream(compressed), len -> len > 1 ? len - 1 : len);
                        os = new ByteArrayOutputStream();
                        compressor.uncompress(is, os);
                        final ByteArrayInputStream bais = new ByteArrayInputStream(os.toByteArray());
                        final MemoryCacheImageInputStream resultIis = new MemoryCacheImageInputStream(bais);
                        resultIis.setByteOrder(byteOrder);
                        final int[] uncompressed = new int[input.length];
                        resultIis.readFully(uncompressed, 0, uncompressed.length);
                        assertThat("Testcase: " + bloscProperties, uncompressed, is(equalTo(input)));
                    }
                }
            }
        }
    }

    private static boolean isBloscAvailable() {
        try {
            new JBlosc(); // fails with e.g. UnsatisfiedLinkException if blosc is not available
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    // Simulates a software.amazon.awssdk.services.s3.checksums.ChecksumValidatingInputStream which
    // does not provide it's own implementation of available() and always returns 0.
    // Additionally, this class may return less bytes than requested from read(), this is allowed per
    // the documentation
    private static class MockAWSChecksumValidatingInputStream extends InputStream {

        private final InputStream in;
        private final Function<Integer, Integer> getBytesRead;

        public MockAWSChecksumValidatingInputStream(InputStream in, Function<Integer, Integer> getBytesRead) {
            this.in = in;
            this.getBytesRead = getBytesRead;
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }

        /*
         * From InputStream:
         *
         * Reads up to <code>len</code> bytes of data from the input stream into
         * an array of bytes.  An attempt is made to read as many as
         * <code>len</code> bytes, but a smaller number may be read.
         */
        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            return in.read(buf, off, getBytesRead.apply(len));
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