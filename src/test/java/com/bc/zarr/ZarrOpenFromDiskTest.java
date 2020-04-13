package com.bc.zarr;

import org.junit.Test;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.Assert.assertArrayEquals;

public class ZarrOpenFromDiskTest {

    @Test
    public void openFromDisk() throws IOException {
        try {
            int X = 425;
            int Y = 450;
            int Z = 24;

            ZarrArray created = ZarrArray.create("docs/examples/output/OpenFromDisk.zarr", new ArrayParams()
                    .shape(Z, X, Y).chunks(8, 250, 250).dataType(DataType.f8).fillValue(Double.NaN).compressor(CompressorFactory.create("blosc", 5))
            );

            double[][][] inputData = new double[Z][X][Y];
            for (int j = 0; j < Z; j++) {
                double[][] dataLayer = new double[X][Y];
                for (int i = 0; i < X; i++) {
                    dataLayer[i] = IntStream.rangeClosed(i * Y + 1, i * Y + Y).mapToDouble(n -> (double) n).toArray();
                }
                inputData[j] = dataLayer;
            }

            int[] writeDataShape = {Z, X, Y};
            int[] offset = {0, 0, 0};
            created.write(inputData, writeDataShape, offset);

            ZarrArray opened = ZarrArray.open("docs/examples/output/OpenFromDisk.zarr");
            int[] readShape = {1, 1, 10};
            final double[] data = (double[]) opened.read(readShape, new int[]{20, 10, 0});

            assertArrayEquals(Arrays.copyOf(inputData[20][10], 10), data, 0);

        } catch (InvalidRangeException e) {
            e.printStackTrace();
        }
    }
}
