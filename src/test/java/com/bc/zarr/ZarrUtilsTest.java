package com.bc.zarr;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

public class ZarrUtilsTest {

    private ZarrHeader _zarrHeader;

    @Before
    public void setUp() throws Exception {
        final int[] chunks = {5, 6};
        final Compressor compressor = CompressorFactory.create("zlib", 1);
        final String dtype = "i4";
        final int[] shape = {10, 15};
        _zarrHeader = new ZarrHeader(shape, chunks, dtype, 3.6d, compressor);

    }

    @Test
    public void toJson() throws IOException {
        final StringWriter writer = new StringWriter();
        ZarrUtils.toJson(_zarrHeader, writer);

        assertThat(strip(writer.toString()), is(equalToIgnoringWhiteSpace(expectedJson())));
    }

    @Test
    public void fromJson() {
        //execution
        final ZarrHeader zarrHeader = ZarrUtils.fromJson(new StringReader(expectedJson()), ZarrHeader.class);

        //verification
        assertNotNull(zarrHeader);
        assertThat(zarrHeader.getChunks(), is(equalTo(_zarrHeader.getChunks())));
        assertThat(zarrHeader.getDtype(), is(equalTo(_zarrHeader.getDtype())));
        assertThat(zarrHeader.getCompressor(), is(equalTo(_zarrHeader.getCompressor())));
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
        assertEquals("1.2.3.42", ZarrUtils.createChunkFilename(new int[]{1, 2, 3, 42}));
    }

    private String expectedJson() {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        pw.println("{");
        pw.println("    \"chunks\": [");
        pw.println("        5,");
        pw.println("        6");
        pw.println("    ],");
        pw.println("    \"compressor\": {");
        pw.println("        \"id\": \"zlib\",");
        pw.println("        \"level\": 1");
        pw.println("    },");
        pw.println("    \"dtype\": \">i4\",");
        pw.println("    \"fill_value\": 3.6,");
        pw.println("    \"filters\": null,");
        pw.println("    \"order\": \"C\",");
        pw.println("    \"shape\": [");
        pw.println("        10,");
        pw.println("        15");
        pw.println("    ],");
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
        assertEquals((long) intSize, longSize);
    }
}