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
package com.bc.zarr;

import com.google.gson.annotations.SerializedName;

import java.nio.ByteOrder;

public class ZarrHeader {

    private final int[] chunks;
    private final CompressorBean compressor;
    private final String dtype;
    private final Number fill_value;
    private final String filters = null;
    private final String order = "C";
    private final int[] shape;
    private final int zarr_format = 2;

    public ZarrHeader(int[] shape, int[] chunks, String dtype, ByteOrder byteOrder, Number fill_value, Compressor compressor) {
        this.chunks = chunks;
        if (compressor == null || CompressorFactory.nullCompressor.equals(compressor)) {
            this.compressor = null;
        } else {
            this.compressor = new CompressorBean(compressor.getId(), compressor.getLevel());
        }

        this.dtype = translateByteOrder(byteOrder) + dtype;
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

    public DataType getRawDataType() {
        String str = dtype;
        str = str.replace(">", "");
        str = str.replace("<", "");
        str = str.replace("|", "");
        return DataType.valueOf(str);
    }

    public ByteOrder getByteOrder() {
        if (dtype.startsWith(">")) {
            return ByteOrder.BIG_ENDIAN;
        } else if (dtype.startsWith("<")) {
            return ByteOrder.LITTLE_ENDIAN;
        } else if (dtype.startsWith("|")) {
            return ByteOrder.nativeOrder();
        }
        return ByteOrder.BIG_ENDIAN;
    }

    private String translateByteOrder(ByteOrder order) {
        if (order == null) {
            order = ByteOrder.nativeOrder();
        }
        if (ByteOrder.BIG_ENDIAN.equals(order)) {
            return ">";
        }
        return "<";
    }

    public Number getFill_value() {
        return fill_value;
    }

    public int[] getShape() {
        return shape;
    }

    public static class CompressorBean {

        private final String id;
        @SerializedName(value = "level", alternate = "clevel")
        private final int level;

        public CompressorBean(String id, int level) {
            this.id = id;
            this.level = level;
        }

        public String getId() {
            return id;
        }

        public int getLevel() {
            return level;
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
    }
}
