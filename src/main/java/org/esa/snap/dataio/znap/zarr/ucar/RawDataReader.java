package org.esa.snap.dataio.znap.zarr.ucar;

import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayLong;
import ucar.ma2.ArrayShort;
import ucar.ma2.InvalidRangeException;

import java.awt.Rectangle;

/**
 * @author sabine.bc
 */
public class RawDataReader {

    public static Array read(Array window, int[] from, Array source) throws InvalidRangeException {

        final int[] windowShape = window.getShape();
        final int windowHeight = windowShape[0];
        final int windowWidth = windowShape[1];

        final int offsetY = from[0];
        final int offsetX = from[1];

        int[] shape = source.getShape();
        final int rawHeight = shape[0];
        final int rawWidth = shape[1];

        boolean windowInside = isWindowInside(offsetX, offsetY, windowWidth, windowHeight, rawWidth, rawHeight);
        if (windowInside) {
            return source.section(from, windowShape);
        }
        return readFrom2DArray(window, from, source);
    }


    public static Array readFrom2DArray(Array target, int[] from, Array source) {
        final Class elementType = source.getElementType();
        if (elementType == double.class) {
            return WindowReader.readWindow((ArrayDouble.D2) target, from, (ArrayDouble.D2) source);
        } else if (elementType == float.class) {
            return WindowReader.readWindow((ArrayFloat.D2) target, from, (ArrayFloat.D2) source);
        } else if (elementType == long.class) {
            return WindowReader.readWindow((ArrayLong.D2) target, from, (ArrayLong.D2) source);
        } else if (elementType == int.class) {
            return WindowReader.readWindow((ArrayInt.D2) target, from, (ArrayInt.D2) source);
        } else if (elementType == short.class) {
            return WindowReader.readWindow((ArrayShort.D2) target, from, (ArrayShort.D2) source);
        } else if (elementType == byte.class) {
            return WindowReader.readWindow((ArrayByte.D2) target, from, (ArrayByte.D2) source);
        } else {
            throw new RuntimeException("Datatype not implemented");
        }
    }

    private static boolean isWindowInside(int winOffSetX, int winOffSetY, int windowWidth, int windowHeight, int rawWidth, int rawHeight) {
        final Rectangle windowRec = new Rectangle(winOffSetX, winOffSetY, windowWidth, windowHeight);
        final Rectangle arrayRectangle = new Rectangle(0, 0, rawWidth, rawHeight);
        return arrayRectangle.contains(windowRec);
    }
}

