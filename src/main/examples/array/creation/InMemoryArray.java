package array.creation;
import com.bc.zarr.*;
import java.io.IOException;

public class InMemoryArray {
    public static void main(String[] args) throws IOException {
        createSimpleSmallArray();
        createSimpleBiggerArray();
    }

    private static void createSimpleSmallArray() throws IOException {
        // snippet 1
        ZarrArray array = ZarrArray.create(new ArrayParams().withShape(10, 8));
        // end 1

        System.out.println(array);
    }

    private static void createSimpleBiggerArray() throws IOException {
        // snippet 2
        ZarrArray array = ZarrArray.create(new ArrayParams().withShape(10000, 8000));
        // end 2

        System.out.println(array);
    }
}

