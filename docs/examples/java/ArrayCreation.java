import com.bc.zarr.*;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import utils.OutputHelper;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import static utils.OutputHelper.createOutput;

public class ArrayCreation {

    public static void main(String[] args) throws IOException, InvalidRangeException {
        example_1();
//        createArrayWithAutomaticallyComputedChunkSize();
//        createArrayWithDisabledChunking();
//        createArrayWithUserDefinedChunks();
//        createArray_WriteAndReadData();
    }

    // Create a simple small zArray
    private static void example_1() throws IOException {
        ZarrArray zarray = ZarrArray.create(new ArrayParams()
                .withShape(10, 8)
        );

        createOutput(ps -> ps.println(zarray));
    }

    private static void createArrayWithAutomaticallyComputedChunkSize() throws IOException {
        // snippet 2
        ZarrArray array = ZarrArray.create(new ArrayParams()
                .withShape(4000, 3500)
        );
        // end 2

        System.out.println("Snippet 2");
        System.out.println(array);
    }

    private static void createArrayWithDisabledChunking() throws IOException {
        // snippet 3
        ZarrArray array = ZarrArray.create(new ArrayParams()
                .withShape(4000, 3500)
                .withChunked(false)
        );
        // end 3

        System.out.println("Snippet 3");
        System.out.println(array);
    }

    private static void createArrayWithUserDefinedChunks() throws IOException {
        // snippet 4
        ZarrArray array = ZarrArray.create(new ArrayParams()
                .withShape(4000, 3500)
                .withChunks(400, 350)
        );
        // end 4

        System.out.println("Snippet 4");
        System.out.println(array);
    }

    private static void createArray_WriteAndReadData() throws IOException, InvalidRangeException {
        // snippet 5
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

        // now we can wrap the data in a ucar.ma2.Array
        final Array ma2Array = Array.factory(entireData).reshape(array.getShape());

        // split it into arrays per line
        final int[][] allLines = (int[][]) ma2Array.copyToNDJavaArray();

        // and print out the lines
        System.out.println("Snippet 5");
        for (int i = 0; i < allLines.length; i++) {
            int[] line = allLines[i];
            System.out.println("line " + i + " = " + Arrays.toString(line));
        }
        // end 5
    }
}

