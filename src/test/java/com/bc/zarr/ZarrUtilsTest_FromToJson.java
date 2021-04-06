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

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ZarrUtilsTest_FromToJson {

    private String expectedJson;

    @Before
    public void setUp() throws Exception {
        expectedJson = strip("{ \"i1\": 2, \"i2\": 3, \"i4\": 4, \"i8\": 5, \"f4\": 2.4, \"f8\": 2.5, \"str\": \"abc\" }");
    }

    @Test
    public void createJsonString() throws JZarrException {
        final HashMap<String, Object> map = new LinkedHashMap<>();
        map.put("i1", (byte) 2);
        map.put("i2", (short) 3);
        map.put("i4", 4);
        map.put("i8", 5L);
        map.put("f4", 2.4F);
        map.put("f8", 2.5);
        map.put("str", "abc");
        final String json = strip(ZarrUtils.toJson(map));
        assertThat(json, equalToIgnoringWhiteSpace(expectedJson));
    }

    @Test
    public void parseJsonString_asMap_convertsAllNumbersToDoubles() throws IOException {
        final LinkedHashMap map = ZarrUtils.fromJson(new StringReader(expectedJson), LinkedHashMap.class);
        assertThat(map.size(), is(7));
        assertThat(map.get("i1"), is(2)); // reading from json file converts numbers to doubles
        assertThat(map.get("i2"), is(3)); // reading from json file converts numbers to doubles
        assertThat(map.get("i4"), is(4)); // reading from json file converts numbers to doubles
        assertThat(map.get("i8"), is(5)); // reading from json file converts numbers to doubles
        assertThat(map.get("f4"), is(2.4)); // reading from json file converts numbers to doubles
        assertThat(map.get("f8"), is(2.5)); // reading from json file converts numbers to doubles
        assertThat(map.get("str"), is("abc"));
    }

    @Test
    public void parseJsonString_asOwnType() throws IOException {
        final MyClass myClass = ZarrUtils.fromJson(new StringReader(expectedJson), MyClass.class);
        assertThat(myClass.i1, is((byte) 2));  // no conversion because the field type is known
        assertThat(myClass.i2, is((short) 3)); // no conversion because the field type is known
        assertThat(myClass.i4, is(4));        // no conversion because the field type is known
        assertThat(myClass.i8, is(5L));       // no conversion because the field type is known
        assertThat(myClass.f4, is(2.4F));     // no conversion because the field type is known
        assertThat(myClass.f8, is(2.5));      // no conversion because the field type is known
        assertThat(myClass.str, is("abc"));
    }

    @Test
    public void toJson_condensedAndPretty() throws JZarrException, IOException {
        //expectations
        final String expected_1 = "{\"ints\":[3,4,5,6,7,8,9],\"floats\":[3.0,4.0,5.0,6.0,7.0,8.0,9.0]}";

        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        pw.println("{");
        pw.println("  \"ints\" : [ 3, 4, 5, 6, 7, 8, 9 ],");
        pw.println("  \"floats\" : [ 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0 ]");
        pw.print("}");
        final String expected_2 = sw.toString();

        //preparation
        final MyArrays myArrays = new MyArrays();
        //execution
        final String toJson = ZarrUtils.toJson(myArrays);
        final MyArrays fromJson = ZarrUtils.fromJson(new StringReader(toJson), MyArrays.class);
        final String toPrettyJson = ZarrUtils.toJson(fromJson, true);

        //verification
        assertThat(toJson, is(equalTo(expected_1)));
        assertThat(toPrettyJson, is(expected_2));
    }

    static class MyArrays {
        public int[] ints = new int[]{3, 4, 5, 6, 7, 8, 9};
        public float[] floats = new float[]{3, 4, 5, 6, 7, 8, 9};
    }

    private static class MyClass {

        public byte i1;
        public short i2;
        public int i4;
        public long i8;
        public float f4;
        public double f8;
        public String str;
    }

    private String strip(String s) {
        return s.replace(" ", "").replace("\n", "").replace("\r ", "");
    }
}