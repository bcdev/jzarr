package org.esa.snap.dataio.znap.zarr;

import static org.junit.Assert.*;

import org.esa.snap.dataio.znap.zarr.ucar.NetCDF_Util;
import org.junit.*;

public class NetCDF_UtilTest {

    @Test
    public void netCDFOrder() {
        //preparation
        final int[] ints = {1, 2, 3, 4, 5};

        //execution
        final int[] netCDFOrder = NetCDF_Util.netCDFOrder(ints);

        //verification
        assertArrayEquals(new int[]{5, 4, 3, 2, 1}, netCDFOrder);
    }
}