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

package com.bc.zarr.ucar;

import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PartialDataCopierTest_3D {

    private final int __ = -1;
    private Array source;
    private Array chunk;

    @Before
    public void setUp() {
        source = Array.factory(DataType.INT, new int[]{3, 3, 3}, new int[]{
                11, 12, 13,
                14, 15, 16,
                17, 18, 19,

                21, 22, 23,
                24, 25, 26,
                27, 28, 29,

                31, 32, 33,
                34, 35, 36,
                37, 38, 39
        });

        chunk = Array.factory(DataType.INT, new int[]{2, 2, 2}, new int[]{
                __, __,
                __, __,

                __, __,
                __, __
        });
    }

    @Test
    public void read3DArraySection_case_inside_with_offset_0() throws InvalidRangeException {
        final int[] from = {0, 0, 0};

        PartialDataCopier.copy(from, source, chunk);

        assertThat(chunk.copyTo1DJavaArray(), equalTo(new int[]{
                11, 12,
                14, 15,

                21, 22,
                24, 25
        }));
    }

    @Test
    public void read3DArraySection_case_inside_with_offset() throws InvalidRangeException {
        final int[] from = {1, 1, 1};

        PartialDataCopier.copy(from, source, chunk);

        assertThat(chunk.copyTo1DJavaArray(), equalTo(new int[]{
                25, 26,
                28, 29,

                35, 36,
                38, 39
        }));
    }

    @Test
    public void read3DArraySection_case_edge_outside_FrontUpperLeft() throws InvalidRangeException {
        final int[] from = {-1, -1, -1};

        PartialDataCopier.copy(from, source, chunk);

        assertThat(chunk.copyTo1DJavaArray(), equalTo(new int[]{
                __, __,
                __, __,

                __, __,
                __, 11
        }));
    }

    @Test
    public void read3DArraySection_case_edge_outside_FrontUpperRight() throws InvalidRangeException {
        final int[] from = {-1, -1, 2};

        PartialDataCopier.copy(from, source, chunk);

        assertThat(chunk.copyTo1DJavaArray(), equalTo(new int[]{
                __, __,
                __, __,

                __, __,
                13, __
        }));
    }

    @Test
    public void read3DArraySection_case_edge_outside_FrontLowerRight() throws InvalidRangeException {
        final int[] from = {-1, 2, 2};

        PartialDataCopier.copy(from, source, chunk);

        assertThat(chunk.copyTo1DJavaArray(), equalTo(new int[]{
                __, __,
                __, __,

                19, __,
                __, __
        }));
    }

    @Test
    public void read3DArraySection_case_edge_outside_FrontLowerLeft() throws InvalidRangeException {
        final int[] from = {-1, 2, -1};

        PartialDataCopier.copy(from, source, chunk);

        assertThat(chunk.copyTo1DJavaArray(), equalTo(new int[]{
                __, __,
                __, __,

                __, 17,
                __, __
        }));
    }

    @Test
    public void read3DArraySection_case_edge_outside_RearUpperLeft() throws InvalidRangeException {
        final int[] from = {2, -1, -1};

        PartialDataCopier.copy(from, source, chunk);

        assertThat(chunk.copyTo1DJavaArray(), equalTo(new int[]{
                __, __,
                __, 31,

                __, __,
                __, __
        }));
    }

    @Test
    public void read3DArraySection_case_edge_outside_RearUpperRight() throws InvalidRangeException {
        final int[] from = {2, -1, 2};

        PartialDataCopier.copy(from, source, chunk);

        assertThat(chunk.copyTo1DJavaArray(), equalTo(new int[]{
                __, __,
                33, __,

                __, __,
                __, __
        }));
    }

    @Test
    public void read3DArraySection_case_edge_outside_RearLowerRight() throws InvalidRangeException {
        final int[] from = {2, 2, 2};

        PartialDataCopier.copy(from, source, chunk);

        assertThat(chunk.copyTo1DJavaArray(), equalTo(new int[]{
                39, __,
                __, __,

                __, __,
                __, __
        }));
    }

    @Test
    public void read3DArraySection_case_edge_outside_RearLowerLeft() throws InvalidRangeException {
        final int[] from = {2, 2, -1};

        PartialDataCopier.copy(from, source, chunk);

        assertThat(chunk.copyTo1DJavaArray(), equalTo(new int[]{
                __, 37,
                __, __,

                __, __,
                __, __
        }));
    }
}