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
package com.bc.zarr.ucar;

import com.bc.zarr.DataType;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;

public class NetCDF_Util {

    public static int[] netCDFOrder(int[] ints) {
        final int length = ints.length;
        final int[] netCDF = new int[length];
        for (int i = 0; i < length; i++) {
            netCDF[i] = ints[length - 1 - i];
        }
        return netCDF;
    }

    public static Array createArrayWithGivenStorage(Object storage, int[] shape) {
        final Class<?> aClass = storage.getClass();
        if (aClass.isArray()) {
            return Array.factory(ucar.ma2.DataType.getType(aClass.getComponentType(), false), shape, storage);
        }
        return null;
    }

    public static Array createFilledArray(ucar.ma2.DataType dataType, int[] shape, Number fill) {
        final Array array = Array.factory(dataType, shape);
        final IndexIterator iter = array.getIndexIterator();
        if (fill != null) {
            if (ucar.ma2.DataType.DOUBLE.equals(dataType)) {
                while (iter.hasNext()) {
                    iter.setDoubleNext(fill.doubleValue());
                }
            } else if (ucar.ma2.DataType.FLOAT.equals(dataType)) {
                while (iter.hasNext()) {
                    iter.setFloatNext(fill.floatValue());
                }
            } else if (ucar.ma2.DataType.INT.equals(dataType)) {
                while (iter.hasNext()) {
                    iter.setIntNext(fill.intValue());
                }
            } else if (ucar.ma2.DataType.SHORT.equals(dataType)) {
                while (iter.hasNext()) {
                    iter.setShortNext(fill.shortValue());
                }
            } else if (ucar.ma2.DataType.BYTE.equals(dataType)) {
                while (iter.hasNext()) {
                    iter.setByteNext(fill.byteValue());
                }
            } else {
                throw new IllegalStateException();
            }
        }
        return array;
    }

    public static ucar.ma2.DataType getDataType(DataType dataType) {
        if (dataType == DataType.f8) {
            return ucar.ma2.DataType.DOUBLE;
        } else if (dataType == DataType.f4) {
            return ucar.ma2.DataType.FLOAT;
        } else if (dataType == DataType.i4 || dataType == DataType.u4) {
            return ucar.ma2.DataType.INT;
        } else if (dataType == DataType.i2 || dataType == DataType.u2) {
            return ucar.ma2.DataType.SHORT;
        }
        return ucar.ma2.DataType.BYTE;
    }
}
