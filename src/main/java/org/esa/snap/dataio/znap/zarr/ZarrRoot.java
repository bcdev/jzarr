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
package org.esa.snap.dataio.znap.zarr;

import static org.esa.snap.dataio.znap.zarr.ZarrConstantsAndUtils.*;

import org.esa.snap.dataio.znap.zarr.chunk.Compressor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ZarrRoot {

    private final Path rootPath;

    public ZarrRoot(Path rootPath) {
        this.rootPath = rootPath;
    }

    public ZarrWriter create(String rastername, ZarrDataType dataType, int[] shape, int[] chunks, Number fillValue, Compressor compressor) throws IOException {
        final ZarrHeader zarrHeader = new ZarrHeader(shape, chunks, dataType.toString(), fillValue, compressor);
        final Path dataPath = this.rootPath.resolve(rastername);
        Files.createDirectories(dataPath);
        final Path headerPath = dataPath.resolve(FILENAME_DOT_ZARRAY);
        try (final BufferedWriter writer = Files.newBufferedWriter(headerPath)) {
            toJson(zarrHeader, writer);
        }

        return new ZarrWriter(dataPath, shape, chunks, dataType, fillValue, compressor);
    }

}
