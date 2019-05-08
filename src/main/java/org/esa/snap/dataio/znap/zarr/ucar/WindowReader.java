/*
 * $Id$
 *
 * Copyright (C) 2015 by Brockmann Consult (info@brockmann-consult.de)
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
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayLong;
import ucar.ma2.ArrayShort;

import java.io.IOException;

public abstract class WindowReader {

    public abstract Array read(int centerX, int centerY, int[] window) throws IOException;

    protected static void fillArray(Array target, int[] from, Array source, RawValueSetter rawSetter) {

        final int[] tShape = target.getShape();
        final int tHeight = tShape[0];
        final int tWidth = tShape[1];

        final int offsetY = from[0];
        final int offsetX = from[1];

        final int[] sShape = source.getShape();
        final int rawHeight = sShape[0];
        final int rawWidth = sShape[1];

        for (int tY = 0; tY < tHeight; tY++) {
            int sY = tY + offsetY;
            for (int tX = 0; tX < tWidth; tX++) {
                int sX = tX + offsetX;
                if ((sY >= 0 && sY < rawHeight) && (sX >= 0 && sX < rawWidth)) {
                    rawSetter.set(tY, tX, sY, sX);
                }
            }
        }
    }

    static Array readWindow(ArrayDouble.D2 target, int[] from, ArrayDouble.D2 source) {
        fillArray(target, from, source,
                  (y, x, yRaw, xRaw) -> {
                      final double value = source.get(yRaw, xRaw);
                      target.set(y, x, value);
                  });
        return target;
    }

    static Array readWindow(ArrayFloat.D2 target, int[] from, ArrayFloat.D2 source) {
        fillArray(target, from, source,
                  (y, x, yRaw, xRaw) -> {
                      final float value = source.get(yRaw, xRaw);
                      target.set(y, x, value);
                  });
        return target;
    }

    static Array readWindow(ArrayLong.D2 target, int[] from, ArrayLong.D2 source) {
        fillArray(target, from, source,
                  (y, x, yRaw, xRaw) -> {
                      final long value = source.get(yRaw, xRaw);
                      target.set(y, x, value);
                  });
        return target;
    }

    static Array readWindow(ArrayInt.D2 target, int[] from, ArrayInt.D2 source) {
        fillArray(target, from, source,
                  (y, x, yRaw, xRaw) -> {
                      final int value = source.get(yRaw, xRaw);
                      target.set(y, x, value);
                  });
        return target;
    }

    static Array readWindow(ArrayShort.D2 target, int[] from, ArrayShort.D2 source) {
        fillArray(target, from, source,
                  (y, x, yRaw, xRaw) -> {
                      final short value = source.get(yRaw, xRaw);
                      target.set(y, x, value);
                  });
        return target;
    }

    static Array readWindow(ArrayByte.D2 target, int[] from, ArrayByte.D2 source) {
        fillArray(target, from, source,
                  (y, x, yRaw, xRaw) -> {
                      final byte value = source.get(yRaw, xRaw);
                      target.set(y, x, value);
                  });
        return target;
    }

    protected interface RawValueSetter {

        void set(int y, int x, int yRaw, int xRaw);
    }
}
