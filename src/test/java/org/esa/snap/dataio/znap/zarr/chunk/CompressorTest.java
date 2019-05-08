package org.esa.snap.dataio.znap.zarr.chunk;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class CompressorTest {

    @Test
    public void writeRead_NullCompressor() throws IOException {
        final Compressor compressor = Compressor.Null;
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
        final Compressor compressor = Compressor.Zip_L1;
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
        compressor.compress(is, os);
        assertThat(os.toByteArray(), is(equalTo(intermediate)));

        //read
        is = new ByteArrayInputStream(intermediate);
        os = new ByteArrayOutputStream();
        compressor.uncompress(is, os);
        assertThat(input, is(equalTo(os.toByteArray())));
    }
}