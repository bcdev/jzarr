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

import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PartialDataCopierTest_1D {

    @Test
    public void copyIntFromCenter() throws InvalidRangeException {
        final Array source = createArray(new int[]{10}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        final Array target = createArray(new int[]{3}, new int[]{0, 0, 0});

        final int[] offset = {3};
        PartialDataCopier.copy(offset, source, target);

        assertThat(target.getStorage(), is(equalTo(new int[]{3, 4, 5})));
    }

    @Test
    public void copyLeftBorder() throws InvalidRangeException {
        final Array source = createArray(new int[]{10}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        final Array target = createArray(new int[]{3}, new int[]{-1, -1, -1});

        final int[] offset = {0};
        PartialDataCopier.copy(offset, source, target);

        assertThat(target.getStorage(), is(equalTo(new int[]{0, 1, 2})));
    }


    @Test
    public void copyLeftOutsideBorder() throws InvalidRangeException {
        final Array source = createArray(new int[]{10}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        final Array target = createArray(new int[]{3}, new int[]{-1, -1, -1});

        final int[] offset = {-1};
        PartialDataCopier.copy(offset, source, target);

        assertThat(target.getStorage(), is(equalTo(new int[]{-1, 0, 1})));
    }

    @Test
    public void copyRightBorder() throws InvalidRangeException {
        final Array source = createArray(new int[]{10}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        final Array target = createArray(new int[]{3}, new int[]{0, 0, 0});

        final int[] offset = {7};
        PartialDataCopier.copy(offset, source, target);

        assertThat(target.getStorage(), is(equalTo(new int[]{7, 8, 9})));
    }


    @Test
    public void copyRightOutsideBorder() throws InvalidRangeException {
        final Array source = createArray(new int[]{10}, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        final Array target = createArray(new int[]{3}, new int[]{0, 0, 0});

        final int[] offset = {8};
        PartialDataCopier.copy(offset, source, target);

        assertThat(target.getStorage(), is(equalTo(new int[]{8, 9, 0})));
    }

    private Array createArray(int[] shape, int[] storage) {
        return Array.factory(DataType.INT, shape, storage);
    }
}