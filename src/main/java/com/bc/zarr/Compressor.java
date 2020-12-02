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

package com.bc.zarr;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class Compressor {

    public abstract String getId();

    public abstract String toString();

    public abstract void compress(InputStream is, OutputStream os) throws IOException;

    public abstract void uncompress(InputStream is, OutputStream os) throws IOException;

    void passThrough(InputStream is, OutputStream os) throws IOException {
        final byte[] bytes = new byte[65536];
        int read = is.read(bytes);
        while (read > 0) {
            os.write(bytes, 0, read);
            read = is.read(bytes);
        }
    }
}
