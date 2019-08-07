package com.bc.zarr.examples;
import com.bc.zarr.*;
import java.io.IOException;

public class CreatingAnArray {
    public static void main(String[] args) throws IOException {
        
        ZarrArray array = ZarrArray.create(new ArrayParams()
                .withShape(10000, 10000).withChunks(1000, 1000).withDataType(DataType.i4)
        );

        System.out.println(array);
    }
}
