/*
 *
 * Copyright (C) 2020 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

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