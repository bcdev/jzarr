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
import com.bc.zarr.JZarrException;
import com.bc.zarr.ZarrArray;

import java.io.IOException;

import static utils.OutputHelper.createOutput;

public class ArrayCreation_rtd {

    public static void main(String[] args) throws IOException, JZarrException {
        example_1();
        example_2();
        example_3();
        example_4();
    }

    /**
     * Create a simple small zArray
     */
    private static void example_1() throws IOException, JZarrException {
        ZarrArray jZarray = ZarrArray.create(new ArrayParams()
                .shape(10, 8)
        );

        createOutput(out -> out.println(jZarray));
    }

    /**
     * Create an array with automatically computed chunk size
     */
    private static void example_2() throws IOException, JZarrException {
        ZarrArray jZarray = ZarrArray.create(new ArrayParams()
                .shape(4000, 3500)
        );

        createOutput(out -> out.println(jZarray));
    }

    /**
     * Create an array with disabled chunking
     */
    private static void example_3() throws IOException, JZarrException {
        ZarrArray jZarray = ZarrArray.create(new ArrayParams()
                .shape(4000, 3500)
                .chunked(false)
        );

        createOutput(out -> out.println(jZarray));
    }

    /**
     * Creates an array with user defined chunks
     */
    private static void example_4() throws IOException, JZarrException {
        ZarrArray jZarray = ZarrArray.create(new ArrayParams()
                .shape(4000, 3500)
                .chunks(400, 350)
        );

        createOutput(out -> out.println(jZarray));
    }
}

