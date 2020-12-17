/*
 *
 * MIT License
 *
 * Copyright (c) 2020. Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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