import com.bc.zarr.ArrayParams;
import com.bc.zarr.CompressorFactory;
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
        example_3();
    }

    /**
     * Creates a 2-dimensional array of 32-bit integers with 10000 rows and 10000 columns, divided into
     * chunks where each chunk has 1000 rows and 1000 columns (and so there will be 100 chunks in total).
     */
    private static void example_1() throws IOException {
        ZarrArray jZarray = ZarrArray.create(new ArrayParams()
                .withShape(10000, 10000)
                .withChunks(1000, 1000)
                .withDataType(DataType.i4)
        );

        createOutput(out -> out.println(jZarray));
    }

    /**
     * Reading and writing data.
     * Creates an array with size 5 * 7 and fill value <code>-1</code>.<br>
     * Then writes data with shape 3 * 5 in the center of the array.<br>
     * Finally read in the entire array data (int[] with size 5 * 7) and we can see
     * the data written before surrounded by the fill value <code>-1</code>.
     */
    private static void example_2() throws IOException, InvalidRangeException {
        ZarrArray jZarray = ZarrArray.create(new ArrayParams()
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
        int[] withThisShape = {3, 5}; // define the shape
        int[] toThisPosition = {1, 1}; // and the place inside the array, where the data should be written

        // write the data
        jZarray.write(dataWeWantToBeWriten, withThisShape, toThisPosition);

        // read entire data
        int[] entireData = (int[]) jZarray.read();

        // Finally we can instantiate for example an org.nd4j.linalg.api.ndarray.INDArray and print out the data
        OutputHelper.Writer writer = out -> {
            DataBuffer buffer = Nd4j.createBuffer(entireData);
            out.println(Nd4j.create(buffer).reshape('c', jZarray.getShape()));
        };

        createOutput(writer);
    }

    /**
     * Creates an array in a local file store.
     */
    public static void example_3() throws IOException, InvalidRangeException {
        // example 3 code snippet 1 begin .. see https://jzarr.readthedocs.io/en/latest/tutorial.html#persistent-arrays
        ZarrArray created = ZarrArray.create("docs/examples/output/example_3.zarr", new ArrayParams()
                .withShape(1000, 1000).withChunks(250, 250).withDataType(DataType.i4).withFillValue(-9999)
        );
        // example 3 code snippet 1 end

        // example 3 code snippet 2 begin .. see https://jzarr.readthedocs.io/en/latest/tutorial.html#persistent-arrays
        created.write(42, new int[]{3, 4}, new int[]{21, 22});
        // example 3 code snippet 2 end

        // example 3 code snippet 3 begin .. see https://jzarr.readthedocs.io/en/latest/tutorial.html#persistent-arrays
        ZarrArray opened = ZarrArray.open("docs/examples/output/example_3.zarr");
        int[] redShape = {5, 6};
        final int[] red = (int[]) opened.read(redShape, new int[]{20, 21});
        // example 3 code snippet 3 end


        createOutput(out -> {
            DataBuffer buffer = Nd4j.createBuffer(red);
            out.println(Nd4j.create(buffer).reshape('c', redShape));
        });
    }

    /**
     * Create an array with an user defined compressor.
     */
    public static void example_4() throws IOException, InvalidRangeException {
        ZarrArray jZarray = ZarrArray.create(new ArrayParams()
                .withShape(243, 324, 742)  // three or more dimensions
                .withCompressor(CompressorFactory.create("zlib", 8)) // 8 : compression level
        );
    }
}
