/*
 * $Id$
 *
 * Copyright (C) 2010 by Brockmann Consult (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation. This program is distributed in the hope it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.esa.snap.dataio.znap.zarr.chunk;

import ucar.ma2.Array;
import ucar.ma2.DataType;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

public class ChunkReaderWriterImpl_Float extends ChunkReaderWriter {

    public ChunkReaderWriterImpl_Float(Compressor compressor, int[] chunkShape, Number fill) {
        super(compressor, chunkShape, fill);
    }

    @Override
    public Array read(Path path) throws IOException {
        if (Files.isRegularFile(path)) {
            try (
                    final InputStream is = Files.newInputStream(path);
                    final ByteArrayOutputStream os = new ByteArrayOutputStream()) {

                compressor.uncompress(is, os);

                final float[] floats = new float[getSize()];
                try (
                        final ByteArrayInputStream bais = new ByteArrayInputStream(os.toByteArray());
                        final ImageInputStream iis = new MemoryCacheImageInputStream(bais)) {
                    iis.setByteOrder(ByteOrder.BIG_ENDIAN);
                    iis.readFully(floats, 0, floats.length);
                }
                return Array.factory(floats).reshape(chunkShape);
            }

        } else {
            return createFilled(DataType.FLOAT);
        }
    }

    @Override
    public void write(Path path, Array array) throws IOException {

        try (
                final ImageOutputStream iis = new MemoryCacheImageOutputStream(new ByteArrayOutputStream());
                final InputStream is = new ZarrInputStreamAdapter(iis);
                final OutputStream os = Files.newOutputStream(path)) {

            final float[] floats = (float[]) array.get1DJavaArray(float.class);
            iis.setByteOrder(ByteOrder.BIG_ENDIAN);
            iis.writeFloats(floats, 0, floats.length);
            iis.seek(0);
            compressor.compress(is, os);
        }
    }
}
