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

package com.bc.zarr.chunk;

//import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import org.junit.*;

import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ZarrInputStreamAdapterTest {

    @Test
    public void read() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final MemoryCacheImageOutputStream stream = new MemoryCacheImageOutputStream(baos);
        final byte i1 = -3;
        final short i2 = -43;
        final int i4 = -63;
        final float f4 = -123.421F;
        final double f8 = -23421.2136E-24;

        stream.writeByte(i1);
        stream.writeShort(i2);
        stream.writeInt(i4);
        stream.writeFloat(f4);
        stream.writeDouble(f8);
        stream.seek(0);

        final ZarrInputStreamAdapter zarrInputStreamAdapter = new ZarrInputStreamAdapter(stream);
        final MemoryCacheImageInputStream is = new MemoryCacheImageInputStream(zarrInputStreamAdapter);

        assertThat(is.readByte(), is(equalTo(i1)));
        assertThat(is.readShort(), is(equalTo(i2)));
        assertThat(is.readInt(), is(equalTo(i4)));
        assertThat(is.readFloat(), is(equalTo(f4)));
        assertThat(is.readDouble(), is(equalTo(f8)));
    }

    @Test
    public void read1() {
    }
}