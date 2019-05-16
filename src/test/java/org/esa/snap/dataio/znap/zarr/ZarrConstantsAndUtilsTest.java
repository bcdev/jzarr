package org.esa.snap.dataio.znap.zarr;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.esa.snap.dataio.znap.zarr.chunk.Compressor;
import org.junit.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

public class ZarrConstantsAndUtilsTest {

    private ZarrHeader _zarrHeader;

    @Before
    public void setUp() throws Exception {
        final int[] chunks = {5, 6};
        final Compressor compressor = Compressor.Zip_L1;
        final String dtype = "i4";
        final int[] shape = {10, 15};
        _zarrHeader = new ZarrHeader(shape, chunks, dtype, 0, compressor);

    }

    @Test
    public void toJson() throws IOException {
        final StringWriter writer = new StringWriter();
        ZarrConstantsAndUtils.toJson(_zarrHeader, writer);

        assertThat(writer.toString(), is(equalToIgnoringWhiteSpace(expectedJson())));
    }

    @Test
    public void fromJson() {
        //execution
        final ZarrHeader zarrHeader = ZarrConstantsAndUtils.fromJson(new StringReader(expectedJson()), ZarrHeader.class);

        //verification
        assertNotNull(zarrHeader);
        assertThat(zarrHeader.getChunks(), is(equalTo(_zarrHeader.getChunks())));
        assertThat(zarrHeader.getDtype(), is(equalTo(_zarrHeader.getDtype())));
        assertThat(zarrHeader.getCompressor(), is(equalTo(_zarrHeader.getCompressor())));
        assertThat(ZarrConstantsAndUtils.getFillValue(zarrHeader), is(equalTo(ZarrConstantsAndUtils.getFillValue(_zarrHeader))));
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
        final int[][] chunkIndices = ZarrConstantsAndUtils.computeChunkIndices(shape, chunks, bufferShape, offset);

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
        final int[][] chunkIndices = ZarrConstantsAndUtils.computeChunkIndices(shape, chunks, bufferShape, offset);

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
        final int[][] chunkIndices = ZarrConstantsAndUtils.computeChunkIndices(shape, chunks, bufferShape, offset);

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
        assertEquals("1.2.3.42", ZarrConstantsAndUtils.createChunkFilename(new int[]{1, 2, 3, 42}));
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
        pw.println("    \"fill_value\": 0,");
        pw.println("    \"filters\": null,");
        pw.println("    \"order\": \"C\",");
        pw.println("    \"shape\": [");
        pw.println("        10,");
        pw.println("        15");
        pw.println("    ],");
        pw.println("    \"zarr_format\": 2");
        pw.println("}");

        return sw.toString();
    }
}