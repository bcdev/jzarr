import com.bc.zarr.ArrayParams;
import com.bc.zarr.DataType;
import com.bc.zarr.ZarrArray;

import java.io.IOException;

import static utils.OutputHelper.createOutput;

public class Tutorial {

    public static void main(String[] args) throws IOException {
        example_1();
    }

    private static void example_1() throws IOException {
        ZarrArray z = ZarrArray.create(new ArrayParams()
                .withShape(10000, 10000)
                .withChunks(1000, 1000)
                .withDataType(DataType.i4)
        );

        createOutput(out -> out.println(z));
    }
}
