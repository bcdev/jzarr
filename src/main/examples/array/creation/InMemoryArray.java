package array.creation;
import com.bc.zarr.*;
import java.io.IOException;

public class InMemoryArray {
    public static void main(String[] args) throws IOException {
        createSimpleArray();
    }

    private static void createSimpleArray() throws IOException {
        // snippet 1
        ZarrArray array = ZarrArray.create(new ArrayParams().withShape(10, 8));
        // end 1

        System.out.println(array);
    }
}

