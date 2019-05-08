package org.esa.snap.dataio.znap.zarr;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.*;

public class ZarrDataTypeTest {

    @Test
    public void test() {
        assertThat(ZarrDataType.valueOf("f8"), is(equalTo(ZarrDataType.f8)));
        assertThat(ZarrDataType.valueOf("f4"), is(equalTo(ZarrDataType.f4)));
        assertThat(ZarrDataType.valueOf("i4"), is(equalTo(ZarrDataType.i4)));
        assertThat(ZarrDataType.valueOf("u4"), is(equalTo(ZarrDataType.u4)));
        assertThat(ZarrDataType.valueOf("i2"), is(equalTo(ZarrDataType.i2)));
        assertThat(ZarrDataType.valueOf("u2"), is(equalTo(ZarrDataType.u2)));
        assertThat(ZarrDataType.valueOf("i1"), is(equalTo(ZarrDataType.i1)));
        assertThat(ZarrDataType.valueOf("u1"), is(equalTo(ZarrDataType.u1)));
    }
}