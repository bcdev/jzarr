package array.creation;
import com.bc.zarr.*;
import java.io.IOException;

public class CreatingAnArray {
    public static void main(String[] args) throws IOException {
        ArrayParams arrayParams = new ArrayParams()
                .withShape(10000, 10000).withChunks(1000, 1000).withDataType(DataType.i4);
        ZarrArray array = ZarrArray.create(arrayParams);

        System.out.println(array);
    }
}

