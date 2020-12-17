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

public class PartialDataCopierTest_2D {

    private Array source;
    private Array chunk;

    @Before
    public void setUp() {
        source = Array.factory(DataType.INT, new int[]{4,5}, new int[]{
                0, 1, 2, 3, 4,
                5, 6, 7, 8, 9,
                10, 11, 12, 13, 14,
                15, 16, 17, 18, 19
        });

        chunk = Array.factory(DataType.INT, new int[]{2, 3}, new int[]{
                40, 40, 40,
                40, 40, 40
        });
    }

    @Test
    public void read2DArraySection_case_inside_center() throws InvalidRangeException {
        final int[] from = {1, 1};

        PartialDataCopier.copy(from, source, chunk);

        assertThat((int[]) chunk.copyTo1DJavaArray(), equalTo(new int[]{
                6, 7, 8,
                11, 12, 13
        }));
    }

    @Test
    public void read2DArraySection_case_inside_UpperLeft() throws InvalidRangeException {
        final int[] from = {0, 0};

        PartialDataCopier.copy(from, source, chunk);

        assertThat((int[]) chunk.copyTo1DJavaArray(), equalTo(new int[]{
                0, 1, 2,
                5, 6, 7
        }));
    }

    @Test
    public void read2DArraySection_case_inside_UpperCenter() throws InvalidRangeException {
        final int[] from = {0, 1};

        PartialDataCopier.copy(from, source, chunk);

        assertThat((int[]) chunk.copyTo1DJavaArray(), equalTo(new int[]{
                1, 2, 3,
                6, 7, 8
        }));
    }

    @Test
    public void read2DArraySection_case_inside_UpperRight() throws InvalidRangeException {
        final int[] from = {0, 2};

        PartialDataCopier.copy(from, source, chunk);

        assertThat((int[]) chunk.copyTo1DJavaArray(), equalTo(new int[]{
                2, 3, 4,
                7, 8, 9
        }));
    }

    @Test
    public void read2DArraySection_case_inside_CenterRight() throws InvalidRangeException {
        final int[] from = {1, 2};

        PartialDataCopier.copy(from, source, chunk);

        assertThat((int[]) chunk.copyTo1DJavaArray(), equalTo(new int[]{
                7, 8, 9,
                12, 13, 14
        }));
    }

    @Test
    public void read2DArraySection_case_inside_LowerRight() throws InvalidRangeException {
        final int[] from = {2, 2};

        PartialDataCopier.copy(from, source, chunk);

        assertThat((int[]) chunk.copyTo1DJavaArray(), equalTo(new int[]{
                12, 13, 14,
                17, 18, 19
        }));
    }

    @Test
    public void read2DArraySection_case_inside_LowerCenter() throws InvalidRangeException {
        final int[] from = {2, 1};

        PartialDataCopier.copy(from, source, chunk);

        assertThat((int[]) chunk.copyTo1DJavaArray(), equalTo(new int[]{
                11, 12, 13,
                16, 17, 18
        }));
    }

    @Test
    public void read2DArraySection_case_inside_LowerLeft() throws InvalidRangeException {
        final int[] from = {2, 0};

        PartialDataCopier.copy(from, source, chunk);

        assertThat((int[]) chunk.copyTo1DJavaArray(), equalTo(new int[]{
                10, 11, 12,
                15, 16, 17
        }));
    }

    @Test
    public void read2DArraySection_case_inside_CenterLeft() throws InvalidRangeException {
        final int[] from = {1, 0};

        PartialDataCopier.copy(from, source, chunk);

        assertThat((int[]) chunk.copyTo1DJavaArray(), equalTo(new int[]{
                5, 6, 7,
                10, 11, 12
        }));
    }

    @Test
    public void read2DArraySection_case_outside_UpperLeft() throws InvalidRangeException {
        final int[] from = {-1, -1};

        PartialDataCopier.copy(from, source, chunk);

        assertThat((int[]) chunk.copyTo1DJavaArray(), equalTo(new int[]{
                40, 40, 40,
                40, 0, 1
        }));
    }

    @Test
    public void read2DArraySection_case_outside_UpperCenter() throws InvalidRangeException {
        final int[] from = {-1, 1};

        PartialDataCopier.copy(from, source, chunk);

        assertThat((int[]) chunk.copyTo1DJavaArray(), equalTo(new int[]{
                40, 40, 40,
                1, 2, 3
        }));
    }

    @Test
    public void read2DArraySection_case_outside_UpperRight() throws InvalidRangeException {
        final int[] from = {-1, 3};

        PartialDataCopier.copy(from, source, chunk);

        assertThat((int[]) chunk.copyTo1DJavaArray(), equalTo(new int[]{
                40, 40, 40,
                3, 4, 40
        }));
    }

    @Test
    public void read2DArraySection_case_outside_CenterRight() throws InvalidRangeException {
        final int[] from = {1, 3};

        PartialDataCopier.copy(from, source, chunk);

        assertThat((int[]) chunk.copyTo1DJavaArray(), equalTo(new int[]{
                8, 9, 40,
                13, 14, 40
        }));
    }

    @Test
    public void read2DArraySection_case_outside_LowerRight() throws InvalidRangeException {
        final int[] from = {3, 3};

        PartialDataCopier.copy(from, source, chunk);

        assertThat((int[]) chunk.copyTo1DJavaArray(), equalTo(new int[]{
                18, 19, 40,
                40, 40, 40
        }));
    }

    @Test
    public void read2DArraySection_case_outside_LowerCenter() throws InvalidRangeException {
        final int[] from = {3,1};

        PartialDataCopier.copy(from, source, chunk);

        assertThat((int[]) chunk.copyTo1DJavaArray(), equalTo(new int[]{
                16, 17, 18,
                40, 40, 40
        }));
    }

    @Test
    public void read2DArraySection_case_outside_LowerLeft() throws InvalidRangeException {
        final int[] from = {3, -1};

        PartialDataCopier.copy(from, source, chunk);

        assertThat((int[]) chunk.copyTo1DJavaArray(), equalTo(new int[]{
                40, 15, 16,
                40, 40, 40
        }));
    }

    @Test
    public void read2DArraySection_case_outside_CenterLeft() throws InvalidRangeException {
        final int[] from = {1, -1};

        PartialDataCopier.copy(from, source, chunk);

        assertThat((int[]) chunk.copyTo1DJavaArray(), equalTo(new int[]{
                40, 5, 6,
                40, 10, 11
        }));
    }
}