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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ChunkReaderWriterImpl_Byte extends ChunkReaderWriter {

    public ChunkReaderWriterImpl_Byte(Compressor compressor, int[] chunkShape, Number fill) {
        super(compressor, chunkShape, fill);
    }

    @Override
    public Array read(Path path) throws IOException {
        if (Files.isRegularFile(path)) {
            try (
                    final InputStream is = Files.newInputStream(path);
                    final ByteArrayOutputStream os = new ByteArrayOutputStream()) {

                compressor.uncompress(is, os);
                final byte[] b = os.toByteArray();
                return Array.factory(b).reshape(chunkShape);
            }

        } else {
            return createFilled(DataType.BYTE);
        }
    }

    @Override
    public void write(Path path, Array array) throws IOException {
        final byte[] bytes = (byte[]) array.get1DJavaArray(byte.class);
        try (final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
             final OutputStream os = Files.newOutputStream(path)) {

            compressor.compress(is, os);
        }
    }
}
