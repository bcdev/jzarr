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

