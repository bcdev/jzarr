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

import org.junit.Before;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PartialDataCopierTest_copyFromChunkToLineTarget {

    private Array targetLine;
    private Array sourceChunk;

    @Before
    public void setUp() {
        sourceChunk = Array.factory(DataType.INT, new int[]{4, 5}, new int[]{
                0, 1, 2, 3, 4,
                5, 6, 7, 8, 9,
                10, 11, 12, 13, 14,
                15, 16, 17, 18, 19
        });

        targetLine = Array.factory(DataType.INT, new int[]{1, 16}, new int[]{
                40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40
        });
    }

    @Test
    public void name() throws InvalidRangeException {
        final int[] to = {2, -4};

        PartialDataCopier.copy(to, sourceChunk, targetLine);

        assertThat((int[]) targetLine.copyTo1DJavaArray(), equalTo(new int[]{
                40, 40, 40, 40, 10, 11, 12, 13, 14, 40, 40, 40, 40, 40, 40, 40
        }));
    }
}