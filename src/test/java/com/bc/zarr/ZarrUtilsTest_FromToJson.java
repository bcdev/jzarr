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
        pw.println("  \"ints\" : [");
        pw.println("    3,");
        pw.println("    4,");
        pw.println("    5,");
        pw.println("    6,");
        pw.println("    7,");
        pw.println("    8,");
        pw.println("    9");
        pw.println("  ],");
        pw.println("  \"floats\" : [");
        pw.println("    3.0,");
        pw.println("    4.0,");
        pw.println("    5.0,");
        pw.println("    6.0,");
        pw.println("    7.0,");
        pw.println("    8.0,");
        pw.println("    9.0");
        pw.println("  ]");
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