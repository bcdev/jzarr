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

        assertThat(target.getStorage(), is(equalTo(new int[]{0,1,2})));
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