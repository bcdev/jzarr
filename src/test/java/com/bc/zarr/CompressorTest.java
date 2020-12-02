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

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.bc.zarr.chunk.ZarrInputStreamAdapter;
import org.junit.*;

import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
}