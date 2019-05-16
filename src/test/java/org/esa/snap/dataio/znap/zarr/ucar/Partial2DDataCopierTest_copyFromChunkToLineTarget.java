package org.esa.snap.dataio.znap.zarr.ucar;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.*;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

public class Partial2DDataCopierTest_copyFromChunkToLineTarget {

    private Array targetLine;
    private Array sourceChunk;

    @Before
    public void setUp() throws Exception {
        sourceChunk = Array.factory(new int[]{
                0, 1, 2, 3, 4,
                5, 6, 7, 8, 9,
                10, 11, 12, 13, 14,
                15, 16, 17, 18, 19
        }).reshape(new int[]{4, 5});

        targetLine = Array.factory(new int[]{
                40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40
        }).reshape(new int[]{1, 16});
    }

    @Test
    public void name() throws InvalidRangeException {
        final int[] to = {2, -4};

        final Array result = Partial2dDataCopier.copy(to, sourceChunk, targetLine);

        assertThat((int[]) result.copyTo1DJavaArray(), equalTo(new int[]{
                40, 40, 40, 40, 10, 11, 12, 13, 14, 40, 40, 40, 40, 40, 40, 40
        }));
    }
}