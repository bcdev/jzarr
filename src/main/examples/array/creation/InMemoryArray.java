package array.creation;

import com.bc.zarr.*;

import java.io.IOException;

public class InMemoryArray {
    public static void main(String[] args) throws IOException {
        createSimpleSmallArray();
        createArrayWithAutomaticallyComputedChunkSize();
        createArrayWithDisabledChunking();
        createArrayWithUserDefinedChunks();
    }

    private static void createSimpleSmallArray() throws IOException {
        // snippet 1
        ZarrArray array = ZarrArray.create(new ArrayParams()
                .withShape(10, 8)
        );
        // end 1

        System.out.println("Snippet 1");
        System.out.println(array);
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
}

