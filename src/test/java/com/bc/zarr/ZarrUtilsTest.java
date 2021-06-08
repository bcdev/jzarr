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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import org.junit.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.ByteOrder;

public class ZarrUtilsTest {

    private ZarrHeader _zarrHeader;

    @Before
    public void setUp() {
        final int[] chunks = {5, 6};
        final Compressor compressor = CompressorFactory.create("zlib", "level", 1);
        final String dtype = "i4";
        final int[] shape = {10, 15};
        _zarrHeader = new ZarrHeader(shape, chunks, dtype, ByteOrder.BIG_ENDIAN, 3.6d, compressor, DimensionSeparator.DOT.getSeparatorChar());
    }

    @Test
    public void toJson() throws IOException {
        final StringWriter writer = new StringWriter();

        ZarrUtils.toJson(_zarrHeader, writer);

        boolean nullCompressor = false;
        assertThat(strip(writer.toString()), is(equalToIgnoringWhiteSpace(expectedJson(nullCompressor))));
    }

    @Test
    public void toJson_withCompressorNull() throws IOException {
        ZarrHeader zarrHeader = new ZarrHeader(_zarrHeader.getShape(),
                                               _zarrHeader.getChunks(),
                                               _zarrHeader.getRawDataType().toString(),
                                               _zarrHeader.getByteOrder(),
                                               _zarrHeader.getFill_value(),
                                               CompressorFactory.nullCompressor,
                                               DimensionSeparator.DOT.getSeparatorChar());
        final StringWriter writer = new StringWriter();

        ZarrUtils.toJson(zarrHeader, writer);

        boolean nullCompressor = true;
        assertThat(strip(writer.toString()), is(equalToIgnoringWhiteSpace(expectedJson(nullCompressor))));
    }

    @Test
    public void fromJson() throws IOException {
        boolean nullCompressor = false;

        //execution
        final ZarrHeader zarrHeader = ZarrUtils.fromJson(new StringReader(expectedJson(nullCompressor)), ZarrHeader.class);

        //verification
        assertNotNull(zarrHeader);
        assertThat(zarrHeader.getChunks(), is(equalTo(_zarrHeader.getChunks())));
        assertThat(zarrHeader.getDtype(), is(equalTo(_zarrHeader.getDtype())));
        assertThat(zarrHeader.getCompressor().toString(), is(equalTo(_zarrHeader.getCompressor().toString())));
        assertThat(zarrHeader.getFill_value().doubleValue(), is(equalTo(_zarrHeader.getFill_value().doubleValue())));
        assertThat(zarrHeader.getShape(), is(equalTo(_zarrHeader.getShape())));
    }

    @Test
    public void computeChunkIndices_1_Indices() {
        //preparation
        final int[] shape = {2000, 3000};
        final int[] chunks = {512, 512};
        final int[] bufferShape = {512, 512};
        final int[] offset = {512, 512};

        //execution
        final int[][] chunkIndices = ZarrUtils.computeChunkIndices(shape, chunks, bufferShape, offset);

        //verification
        assertNotNull(chunkIndices);
        assertEquals(1, chunkIndices.length);
        assertArrayEquals(new int[]{1, 1}, chunkIndices[0]);
    }

    @Test
    public void computeChunkIndices_2_Indices() {
        //preparation
        final int[] shape = {2000, 3000};
        final int[] chunks = {512, 512};
        final int[] bufferShape = {512, 512};
        final int[] offset = {512, 600};

        //execution
        final int[][] chunkIndices = ZarrUtils.computeChunkIndices(shape, chunks, bufferShape, offset);

        //verification
        assertNotNull(chunkIndices);
        assertEquals(2, chunkIndices.length);
        assertArrayEquals(new int[]{1, 1}, chunkIndices[0]);
        assertArrayEquals(new int[]{1, 2}, chunkIndices[1]);
    }

    @Test
    public void computeChunkIndices_4_Indices() {
        //preparation
        final int[] shape = {2000, 3000};
        final int[] chunks = {512, 512};
        final int[] bufferShape = {512, 512};
        final int[] offset = {600, 600};

        //execution
        final int[][] chunkIndices = ZarrUtils.computeChunkIndices(shape, chunks, bufferShape, offset);

        //verification
        assertNotNull(chunkIndices);
        assertEquals(4, chunkIndices.length);
        assertArrayEquals(new int[]{1, 1}, chunkIndices[0]);
        assertArrayEquals(new int[]{1, 2}, chunkIndices[1]);
        assertArrayEquals(new int[]{2, 1}, chunkIndices[2]);
        assertArrayEquals(new int[]{2, 2}, chunkIndices[3]);
    }

    @Test
    public void computeChunkFilename() {
        assertEquals("1.2.3.42", ZarrUtils.createChunkFilename(new int[]{1, 2, 3, 42}, "."));
    }

    private String expectedJson(boolean nullCompressor) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        pw.println("{");
        pw.println("    \"chunks\": [");
        pw.println("        5,");
        pw.println("        6");
        pw.println("    ],");
        if (nullCompressor) {
            pw.println("    \"compressor\": null,");
        } else {
            pw.println("    \"compressor\": {");
            pw.println("        \"level\": 1,");
            pw.println("        \"id\": \"zlib\"");
            pw.println("    },");
        }
        pw.println("    \"dtype\": \">i4\",");
        pw.println("    \"fill_value\": 3.6,");
        pw.println("    \"filters\": null,");
        pw.println("    \"order\": \"C\",");
        pw.println("    \"shape\": [");
        pw.println("        10,");
        pw.println("        15");
        pw.println("    ],");
        pw.println("    \"dimension_separator\": \".\",");
        pw.println("    \"zarr_format\": 2");
        pw.println("}");

        return strip(sw.toString());
    }

    private String strip(String s) {
        s = s.replace("\r", "").replace("\n", "");
        s = s.replace(" ", "");
//        while (s.contains("  ")) s = s.replace("  ", " ");
        return s;
    }

    @Test
    public void computeSize() {
        final int intSize = ZarrUtils.computeSizeInteger(new int[]{2, 3, 4});
        final long longSize = ZarrUtils.computeSize(new int[]{2, 3, 4});
        assertEquals(24, intSize);
        assertEquals(intSize, longSize);
    }
}