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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ZarrUtilsTestCompression {

    private ZarrHeader _zarrHeader;
    private final String compression;

    @Parameterized.Parameters
    public static Collection<String[]> getCompressions() {
        return Arrays.asList(new String[][] {
            {"blosc"},
            {"zlib"},
            {"null"}
        });
    }

    public ZarrUtilsTestCompression(String compression) {
        this.compression = compression;
    }

    @Before
    public void setUp() {
        final int[] chunks = {5, 6};
        final Compressor compressor = CompressorFactory.create(compression);
        final String dtype = "i4";
        final int[] shape = {10, 15};
        _zarrHeader = new ZarrHeader(shape, chunks, dtype, ByteOrder.BIG_ENDIAN, 3.6d, compressor, DimensionSeparator.DOT.getSeparatorChar());
    }

    @Test
    public void toJson() throws IOException {
        final StringWriter writer = new StringWriter();

        ZarrUtils.toJson(_zarrHeader, writer);
        assertThat(strip(writer.toString()), is(equalToIgnoringWhiteSpace(expectedJson(compression))));
    }


    @Test
    public void fromJson() throws IOException {
        //execution
        final ZarrHeader zarrHeader = ZarrUtils.fromJson(new StringReader(expectedJson(compression)), ZarrHeader.class);

        //verification
        assertNotNull(zarrHeader);
        assertThat(zarrHeader.getChunks(), is(equalTo(_zarrHeader.getChunks())));
        assertThat(zarrHeader.getDtype(), is(equalTo(_zarrHeader.getDtype())));
        if (compression.equals("null")) {
            assertNull(zarrHeader.getCompressor());
        } else {
            assertNotNull(zarrHeader.getCompressor());
            assertThat(zarrHeader.getCompressor().toString(), is(equalTo(_zarrHeader.getCompressor().toString())));
        }
        assertThat(zarrHeader.getFill_value().doubleValue(), is(equalTo(_zarrHeader.getFill_value().doubleValue())));
        assertThat(zarrHeader.getShape(), is(equalTo(_zarrHeader.getShape())));
    }


    private String expectedJson(String compression) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        pw.println("{");
        pw.println("    \"chunks\": [");
        pw.println("        5,");
        pw.println("        6");
        pw.println("    ],");
        if (compression.equals("null")) {
            pw.println("    \"compressor\": null,");
        } else if (compression.equals("zlib")) {
            pw.println("    \"compressor\": {");
            pw.println("        \"level\": 1,");
            pw.println("        \"id\": \"zlib\"");
            pw.println("    },");
        } else if (compression.equals("blosc")) {
            pw.println("    \"compressor\": {");
            pw.println("        \"clevel\": 5,");
            pw.println("        \"blocksize\": 0,");
            pw.println("        \"shuffle\": 1,");
            pw.println("        \"cname\": \"lz4\",");
            pw.println("        \"id\": \"blosc\"");
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

}
