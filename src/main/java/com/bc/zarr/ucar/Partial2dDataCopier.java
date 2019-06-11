package com.bc.zarr.ucar;

import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayLong;
import ucar.ma2.ArrayShort;
import ucar.ma2.InvalidRangeException;

/**
 * @author sabine.bc
 */
public class Partial2dDataCopier {

    public static Array copy(int[] offset, Array source, Array target) throws InvalidRangeException {

        final int[] targetShape = target.getShape();
        final int targetHeight = targetShape[0];
        final int targetWidth = targetShape[1];

        final int offsetY = offset[0];
        final int offsetX = offset[1];

        final int[] sourceShape = source.getShape();
        final int sourceHeight = sourceShape[0];
        final int sourceWidth = sourceShape[1];

        final ValueSetter valueSetter = createValueSetter(source, target);

        for (int sourceY = 0; sourceY < sourceHeight; sourceY++) {
            int targetY = sourceY - offsetY;
            if (targetY < 0 || targetY >= targetHeight) {
                continue;
            }
            for (int sourceX = 0; sourceX < sourceWidth; sourceX++) {
                int targetX = sourceX - offsetX;
                if (targetX < 0 || targetX >= targetWidth) {
                    continue;
                }
                valueSetter.set(targetY, targetX, sourceY, sourceX);
            }
        }
        return target;
    }

    private static ValueSetter createValueSetter(Array source, Array target) {
        final Class elementType = source.getElementType();
        if (elementType == double.class) {
            final ArrayDouble.D2 source2D = (ArrayDouble.D2) source;
            final ArrayDouble.D2 target2D = (ArrayDouble.D2) target;
            return (targetY, targetX, sourceY, sourceX) -> {
                target2D.set(targetY, targetX, source2D.get(sourceY, sourceX));
            };
        } else if (elementType == float.class) {
            final ArrayFloat.D2 source2D = (ArrayFloat.D2) source;
            final ArrayFloat.D2 target2D = (ArrayFloat.D2) target;
            return (targetY, targetX, sourceY, sourceX) -> {
                target2D.set(targetY, targetX, source2D.get(sourceY, sourceX));
            };
        } else if (elementType == long.class) {
            final ArrayLong.D2 source2D = (ArrayLong.D2) source;
            final ArrayLong.D2 target2D = (ArrayLong.D2) target;
            return (targetY, targetX, sourceY, sourceX) -> {
                target2D.set(targetY, targetX, source2D.get(sourceY, sourceX));
            };
        } else if (elementType == int.class) {
            final ArrayInt.D2 source2D = (ArrayInt.D2) source;
            final ArrayInt.D2 target2D = (ArrayInt.D2) target;
            return (targetY, targetX, sourceY, sourceX) -> {
                target2D.set(targetY, targetX, source2D.get(sourceY, sourceX));
            };
        } else if (elementType == short.class) {
            final ArrayShort.D2 source2D = (ArrayShort.D2) source;
            final ArrayShort.D2 target2D = (ArrayShort.D2) target;
            return (targetY, targetX, sourceY, sourceX) -> {
                target2D.set(targetY, targetX, source2D.get(sourceY, sourceX));
            };
        } else if (elementType == byte.class) {
            final ArrayByte.D2 source2D = (ArrayByte.D2) source;
            final ArrayByte.D2 target2D = (ArrayByte.D2) target;
            return (targetY, targetX, sourceY, sourceX) -> {
                target2D.set(targetY, targetX, source2D.get(sourceY, sourceX));
            };
        }
        throw new RuntimeException("Datatype not implemented");
    }

    private interface ValueSetter {

        void set(int targetY, int targetX, int sourceY, int sourceX);
    }
}

