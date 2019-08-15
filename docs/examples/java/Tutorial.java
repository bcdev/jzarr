import com.bc.zarr.ArrayParams;
import com.bc.zarr.DataType;
import com.bc.zarr.ZarrArray;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.factory.Nd4j;
import ucar.ma2.InvalidRangeException;
import utils.OutputHelper;

import java.io.IOException;

import static utils.OutputHelper.createOutput;

public class Tutorial {

    public static void main(String[] args) throws IOException, InvalidRangeException {
        example_1();
        example_2();
    }

    /**
     * Creates a 2-dimensional array of 32-bit integers with 10000 rows and 10000 columns, divided into
     * chunks where each chunk has 1000 rows and 1000 columns (and so there will be 100 chunks in total).
     */
    private static void example_1() throws IOException {
        ZarrArray z = ZarrArray.create(new ArrayParams()
                .withShape(10000, 10000)
                .withChunks(1000, 1000)
                .withDataType(DataType.i4)
        );

        createOutput(out -> out.println(z));
    }

    /**
     * Reading and writing data.
     * Creates an array with size 5 * 7 and fill value <code>-1</code>.<br>
     * Then writes data with shape 3 * 5 in the center of the array.<br>
     * Finally read in the entire array data (int[] with size 5 * 7) and we can see
     * the data written before surrounded by the fill value <code>-1</code>.
     */
    private static void example_2() throws IOException, InvalidRangeException {
        ZarrArray array = ZarrArray.create(new ArrayParams()
                .withShape(5, 7)
                .withDataType(DataType.i4) // integer data type
                .withFillValue(-9999)
        );

        // define data to be written
        int[] dataWeWantToBeWriten = {
                11, 12, 13, 14, 15,
                21, 22, 23, 24, 25,
                31, 32, 33, 34, 35
        };
        final int[] withThisShape = {3, 5}; // define the shape
        final int[] toThisPosition = {1, 1}; // and the place inside the array, where the data should be written

        // write the data
        array.write(dataWeWantToBeWriten, withThisShape, toThisPosition);

        // read entire data
        final int[] entireData = (int[]) array.read();

        // Finally we can instantiate for example an org.nd4j.linalg.api.ndarray.INDArray and print out the data
        final OutputHelper.Writer writer = out -> {
            final DataBuffer buffer = Nd4j.createBuffer(entireData);
            out.println(Nd4j.create(buffer).reshape('c', array.getShape()));
        };

        createOutput(writer);
    }
}
