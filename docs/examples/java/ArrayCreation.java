import com.bc.zarr.ArrayParams;
import com.bc.zarr.ZarrArray;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;

import static utils.OutputHelper.createOutput;

public class ArrayCreation {

    public static void main(String[] args) throws IOException, InvalidRangeException {
        example_1();
        example_2();
        example_3();
        example_4();
    }

    /**
     * Create a simple small zArray
     */
    private static void example_1() throws IOException {
        ZarrArray jZarray = ZarrArray.create(new ArrayParams()
                .withShape(10, 8)
        );

        createOutput(out -> out.println(jZarray));
    }

    /**
     * Create an array with automatically computed chunk size
     */
    private static void example_2() throws IOException {
        ZarrArray jZarray = ZarrArray.create(new ArrayParams()
                .withShape(4000, 3500)
        );

        createOutput(out -> out.println(jZarray));
    }

    /**
     * Create an array with disabled chunking
     */
    private static void example_3() throws IOException {
        ZarrArray jZarray = ZarrArray.create(new ArrayParams()
                .withShape(4000, 3500)
                .withChunked(false)
        );

        createOutput(out -> out.println(jZarray));
    }

    /**
     * Creates an array with user defined chunks
     */
    private static void example_4() throws IOException {
        ZarrArray jZarray = ZarrArray.create(new ArrayParams()
                .withShape(4000, 3500)
                .withChunks(400, 350)
        );

        createOutput(out -> out.println(jZarray));
    }
}

