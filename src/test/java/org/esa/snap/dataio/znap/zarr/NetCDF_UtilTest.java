package org.esa.snap.dataio.znap.zarr;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.sun.medialib.mlib.mediaLibImageInterpTable;
import org.esa.snap.dataio.znap.zarr.ucar.NetCDF_Util;
import org.junit.*;
import ucar.ma2.Array;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayShort;
import ucar.ma2.Index;

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

    @Test
    public void createArrayWithGivenStorage_byte() {
        final byte[] storage = {1, 2, 3, 4, 5, 6};
        final int[] shape = {2, 3};

        Array array = NetCDF_Util.createArrayWithGivenStorage(storage, shape);
        final Index index = array.getIndex();
        index.set(1, 2);
        array.setDouble(index, 111.111);

        assertThat(array, is(instanceOf(ArrayByte.D2.class)));
        assertThat(array.getShape(), is(equalTo(shape)));
        assertThat(array.getStorage(), is(sameInstance(storage)));
        assertThat(array.getStorage(), is(equalTo(new byte[]{1, 2, 3, 4, 5, 111})));
    }

    @Test
    public void createArrayWithGivenStorage_short() {
        final short[] storage = {1, 2, 3, 4, 5, 6};
        final int[] shape = {2, 3};

        Array array = NetCDF_Util.createArrayWithGivenStorage(storage, shape);
        final Index index = array.getIndex();
        index.set(1, 2);
        array.setDouble(index, 111.111);

        assertThat(array, is(instanceOf(ArrayShort.D2.class)));
        assertThat(array.getShape(), is(equalTo(shape)));
        assertThat(array.getStorage(), is(sameInstance(storage)));
        assertThat(array.getStorage(), is(equalTo(new short[]{1, 2, 3, 4, 5, 111})));
    }

    @Test
    public void createArrayWithGivenStorage_int() {
        final int[] storage = {1, 2, 3, 4, 5, 6};
        final int[] shape = {2, 3};

        Array array = NetCDF_Util.createArrayWithGivenStorage(storage, shape);
        final Index index = array.getIndex();
        index.set(1, 2);
        array.setDouble(index, 111.111);

        assertThat(array, is(instanceOf(ArrayInt.D2.class)));
        assertThat(array.getShape(), is(equalTo(shape)));
        assertThat(array.getStorage(), is(sameInstance(storage)));
        assertThat(array.getStorage(), is(equalTo(new int[]{1, 2, 3, 4, 5, 111})));
    }

    @Test
    public void createArrayWithGivenStorage_float() {
        final float[] storage = {1.1f, 2.1f, 3.1f, 4.1f, 5.1f, 6.1f};
        final int[] shape = {2, 3};

        Array array = NetCDF_Util.createArrayWithGivenStorage(storage, shape);
        final Index index = array.getIndex();
        index.set(1, 2);
        array.setDouble(index, 111.111);

        assertThat(array, is(instanceOf(ArrayFloat.D2.class)));
        assertThat(array.getShape(), is(equalTo(shape)));
        assertThat(array.getStorage(), is(sameInstance(storage)));
        assertThat(array.getStorage(), is(equalTo(new float[]{1.1f, 2.1f, 3.1f, 4.1f, 5.1f, 111.111f})));
    }

    @Test
    public void createArrayWithGivenStorage_double() {
        final double[] storage = {1.1, 2.1, 3.1, 4.1, 5.1, 6.1};
        final int[] shape = {2, 3};

        Array array = NetCDF_Util.createArrayWithGivenStorage(storage, shape);
        final Index index = array.getIndex();
        index.set(1, 2);
        array.setDouble(index, 111.111);

        assertThat(array, is(instanceOf(ArrayDouble.D2.class)));
        assertThat(array.getShape(), is(equalTo(shape)));
        assertThat(array.getStorage(), is(sameInstance(storage)));
        assertThat(array.getStorage(), is(equalTo(new double[]{1.1, 2.1, 3.1, 4.1, 5.1, 111.111})));
    }
}