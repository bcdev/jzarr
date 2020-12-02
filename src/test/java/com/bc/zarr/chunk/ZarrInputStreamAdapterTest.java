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