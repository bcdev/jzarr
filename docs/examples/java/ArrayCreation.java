import com.bc.zarr.ArrayParams;
import com.bc.zarr.DataType;
import com.bc.zarr.ZarrArray;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import ucar.ma2.InvalidRangeException;
import utils.OutputHelper.Writer;

import java.io.IOException;

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
        int[] dataWeWantToBeWriten = {
                11, 12, 13, 14, 15,
                21, 22, 23, 24, 25,
                31, 32, 33, 34, 35
        };
        final int[] withShape = {3, 5}; // define the shape
        final int[] toPosition = {1, 1}; // and the place inside the array, where the data should be written

        // write the data
        array.write(dataWeWantToBeWriten, withShape, toPosition);

        // read entire data
        final int[] entireData = new int[5 * 7];
        array.read(entireData, array.getShape());

        // Finally we can instantiate for example an org.nd4j.linalg.api.ndarray.INDArray and print out the data
        final Writer writer = out -> {
            final INDArray nd4j = Nd4j.create(Nd4j.createBuffer(entireData)).reshape('c', array.getShape());
            out.println(nd4j);
        };

        createOutput(writer);
    }
}

