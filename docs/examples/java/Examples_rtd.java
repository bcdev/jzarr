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

import com.bc.zarr.ArrayParams;
import com.bc.zarr.ZarrArray;
import com.bc.zarr.DataType;
import com.bc.zarr.ZarrGroup;
import com.bc.zarr.storage.InMemoryStore;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.util.*;

import static utils.OutputHelper.*;

public class Examples_rtd {

    public static void main(String[] args) throws IOException, InvalidRangeException {
        creatingAnArray();
        createGroupAndSubgroups();
        createArrayAndReadData();
        createArrayAndWriteData();
    }

    private static void creatingAnArray() throws IOException {
        printHeadline();

        ZarrArray array = ZarrArray.create(new ArrayParams().shape(10000, 10000).chunks(1000, 1000).dataType(DataType.i4));

        System.out.println(array);
    }

    private static void createArrayAndWriteData() throws IOException, InvalidRangeException {
        printHeadline();

        final ArrayParams arrayParams = new ArrayParams();
        arrayParams.shape(4, 5).dataType(DataType.f4).fillValue(-1);
        final ZarrArray array = ZarrArray.create(new InMemoryStore(), arrayParams);

        final int[] writeTo = {1, 1};
        final int[] shape = {2, 3};
        final int[] data = {11, 22, 33, 44, 55, 66};
        array.write(data, shape, writeTo);

        final float[] allData = new float[20];
        array.read(allData, array.getShape());

        final float[][] floatArrays = (float[][]) Array.factory(ucar.ma2.DataType.FLOAT, array.getShape(), allData).copyToNDJavaArray();
        for (float[] floats : floatArrays) {
            System.out.println(Arrays.toString(floats));
        }
    }

    private static void createArrayAndReadData() throws IOException, InvalidRangeException {
        printHeadline();

        final ArrayParams arrayParams = new ArrayParams();
        arrayParams.shape(3000, 4000).fillValue(3);
        final ZarrArray array = ZarrArray.create(new InMemoryStore(), arrayParams);

        final int[] data = new int[6];
        final int[] dataShape = {2, 3};
        final int[] readFrom = {11, 12};
        array.read(data, dataShape, readFrom);

        final int[] shape = array.getShape();
        final String sString = Arrays.toString(shape);
        System.out.println("shape = " + sString);
        System.out.println("chunks = " + Arrays.toString(array.getChunks()));
        System.out.println("fillValue = " + array.getFillValue());
        System.out.println("dataType = " + array.getDataType());
        System.out.println("byteOrder = " + array.getByteOrder());
        System.out.println("data sample: " + Arrays.toString(data));
    }

    private static void createGroupAndSubgroups() throws IOException {
        printHeadline();

        final ZarrGroup rootGroup = ZarrGroup.create(new InMemoryStore());
        final ZarrGroup sub1 = rootGroup.createSubGroup("sub1");
        final ZarrGroup sub2 = rootGroup.createSubGroup("sub2");
        final ZarrGroup sub3 = sub2.createSubGroup("sub3");

        final Set<String> groupKeys = rootGroup.getGroupKeys();
        for (String groupKey : groupKeys) {
            System.out.println(groupKey);
        }
    }

}
