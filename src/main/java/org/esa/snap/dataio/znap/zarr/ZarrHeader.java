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

import org.esa.snap.dataio.znap.zarr.chunk.Compressor;

public class ZarrHeader {

    private int[] chunks;
    private CompressorBean compressor;
    private String dtype;
    private Number fill_value;
    private final String filters = null;
    private final String order = "C";
    private int[] shape;
    private final int zarr_format = 2;

    public ZarrHeader() {
    }

    public ZarrHeader(int[] shape, int[] chunks, String dtype, Number fill_value, Compressor compressor) {
        this.chunks = chunks;
        if (Compressor.Null.equals(compressor)) {
            this.compressor = null;
        } else {
            this.compressor = new CompressorBean(compressor.getId(), compressor.getLevel());
        }
        this.dtype = ">" + dtype;
        this.fill_value = fill_value;
        this.shape = shape;
    }

    public int[] getChunks() {
        return chunks;
    }

    public CompressorBean getCompressor() {
        return compressor;
    }

    public String getDtype() {
        return dtype;
    }

    public Number getFill_value() {
        return fill_value;
    }

    public int[] getShape() {
        return shape;
    }

    private class CompressorBean{
        private final String id;
        private final int level;

        public CompressorBean(String id, int level) {
            this.id = id;
            this.level = level;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            CompressorBean that = (CompressorBean) o;

            if (level != that.level) {
                return false;
            }
            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            int result = id.hashCode();
            result = 31 * result + level;
            return result;
        }
    }
}
