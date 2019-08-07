package com.bc.zarr;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.*;

public class DataTypeTest {

    @Test
    public void test() {
        assertThat(DataType.valueOf("f8"), is(equalTo(DataType.f8)));
        assertThat(DataType.valueOf("f4"), is(equalTo(DataType.f4)));
        assertThat(DataType.valueOf("i4"), is(equalTo(DataType.i4)));
        assertThat(DataType.valueOf("u4"), is(equalTo(DataType.u4)));
        assertThat(DataType.valueOf("i2"), is(equalTo(DataType.i2)));
        assertThat(DataType.valueOf("u2"), is(equalTo(DataType.u2)));
        assertThat(DataType.valueOf("i1"), is(equalTo(DataType.i1)));
        assertThat(DataType.valueOf("u1"), is(equalTo(DataType.u1)));
    }
}