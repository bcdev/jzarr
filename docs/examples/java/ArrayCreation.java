import com.bc.zarr.*;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.util.Arrays;

import static utils.OutputHelper.createOutput;

public class ArrayCreation {

    public static void main(String[] args) throws IOException, InvalidRangeException {
        example_1();
        example_2();
        example_3();
        example_4();
        example_5();
    }

    /**
     * Create a simple small zArray
     */
    private static void example_1() throws IOException {
        ZarrArray zarray = ZarrArray.create(new ArrayParams()
                .withShape(10, 8)
        );

        createOutput(out -> out.println(zarray));
    }

    /**
     * Create an array with automatically computed chunk size
     */
    private static void example_2() throws IOException {
        ZarrArray array = ZarrArray.create(new ArrayParams()
                .withShape(4000, 3500)
        );

        createOutput(out -> out.println(array));
    }

    /**
     * Create an array with disabled chunking
     */
    private static void example_3() throws IOException {
        ZarrArray array = ZarrArray.create(new ArrayParams()
                .withShape(4000, 3500)
                .withChunked(false)
        );

        createOutput(out -> out.println(array));
    }

    /**
     * Creates an array with user defined chunks
     */
    private static void example_4() throws IOException {
        ZarrArray array = ZarrArray.create(new ArrayParams()
                .withShape(4000, 3500)
                .withChunks(400, 350)
        );

        createOutput(out -> out.println(array));
    }

    /**
     * Creates an array with size 5 * 7 and fill value <code>-1</code>.<br>
     * Then writes data with shape 3 * 5 in the center of the array.<br>
     * Finally read in the entire array data (int[] with size 5 * 7) and we can see
     * the data written before surrounded by the fill value <code>-1</code>.
     */
    private static void example_5() throws IOException, InvalidRangeException {
        ZarrArray array = ZarrArray.create(new ArrayParams()
                .withShape(5, 7)
                .withDataType(DataType.i4) // integer data type
                .withFillValue(-1)
        );

        // define data to be written
        int[] dataWeWantWrite = {
                11, 12, 13, 14, 15,
                21, 22, 23, 24, 25,
                31, 32, 33, 34, 35
        };
        final int[] withShape = {3, 5};
        final int[] toPosition = {1, 1};

        // write the data
        array.write(dataWeWantWrite, withShape, toPosition);

        // read entire data
        final int[] entireData = new int[5 * 7];
        array.read(entireData, array.getShape());

        createOutput(out -> {
            // now we can wrap the data in a ucar.ma2.Array
            final Array ma2Array = Array.factory(entireData).reshape(array.getShape());

            // split it into arrays per line
            final int[][] allLines = (int[][]) ma2Array.copyToNDJavaArray();

            // and print out the lines
            for (int[] line : allLines) {
                out.println(Arrays.toString(line));
            }
        });
    }
}

