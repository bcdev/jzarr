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
package org.esa.snap.dataio.znap.zarr.ucar;

import ucar.ma2.Array;
import ucar.ma2.DataType;
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

    public static Array createArrayWithGivenStorage(Object storage, int[] shape ) {
        final Class<?> aClass = storage.getClass();
        if (aClass.isArray()){
                return Array.factory(storage.getClass().getComponentType(), shape, storage);
        }
        return null;
    }

    public static Array createFilledArray(DataType dataType, int[] shape, Number fill) {
        final Array array = Array.factory(dataType, shape);
        final IndexIterator iter = array.getIndexIterator();
        if (DataType.DOUBLE.equals(dataType)) {
            while (iter.hasNext()) {
                iter.setDoubleNext(fill.doubleValue());
            }
        } else if (DataType.FLOAT.equals(dataType)) {
            while (iter.hasNext()) {
                iter.setFloatNext(fill.floatValue());
            }
        } else if (DataType.INT.equals(dataType)) {
            while (iter.hasNext()) {
                iter.setIntNext(fill.intValue());
            }
        } else if (DataType.SHORT.equals(dataType)) {
            while (iter.hasNext()) {
                iter.setShortNext(fill.shortValue());
            }
        } else if (DataType.BYTE.equals(dataType)) {
            while (iter.hasNext()) {
                iter.setByteNext(fill.byteValue());
            }
        } else {
            throw new IllegalStateException();
        }
        return array;
    }
}
