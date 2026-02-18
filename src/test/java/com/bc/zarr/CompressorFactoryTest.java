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

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class CompressorFactoryTest {

    @Test
    public void getDefaultCompressorProperties() {
        final Map<String, Object> map = CompressorFactory.getDefaultCompressorProperties();
        assertNotNull(map);
        assertEquals(6, map.size());
        assertThat(map.containsKey("id"), is(true));
        assertThat(map.containsKey("cname"), is(true));
        assertThat(map.containsKey("clevel"), is(true));
        assertThat(map.containsKey("blocksize"), is(true));
        assertThat(map.containsKey("shuffle"), is(true));
        assertThat(map.containsKey("nthreads"), is(true));

        assertThat(map.get("id"), is("blosc"));
        assertThat(map.get("cname"), is("lz4"));
        assertThat(map.get("clevel"), is(5));
        assertThat(map.get("blocksize"), is(0));
        assertThat(map.get("shuffle"), is(1));
        assertThat(map.get("nthreads"), is(1));
    }

    @Test
    public void create_with_id_and_key_value_arguments() {
        final Compressor compressor = CompressorFactory.create("zlib", "level", 4);
        assertNotNull(compressor);
        assertEquals("zlib", compressor.getId());
        assertEquals("compressor=zlib/level=4", compressor.toString());
    }

    @Test
    public void create_with_id_and_key_value_arguments__nullCompressor() {
        final Compressor compressor = CompressorFactory.create("null");
        assertNotNull(compressor);
        assertEquals(null, compressor.getId());
        assertEquals(null, compressor.toString());
    }

    @Test
    public void create_with_map() {
        final Compressor compressor = CompressorFactory.create(TestUtils.createMap("id", "zlib", "level", 4));
        assertNotNull(compressor);
        assertEquals("zlib", compressor.getId());
        assertEquals("compressor=zlib/level=4", compressor.toString());
    }

    @Test
    public void create_zlib_levelValid() {
        final Compressor compressor = CompressorFactory.create("zlib", TestUtils.createMap("level", 1));
        assertNotNull(compressor);
        assertEquals("zlib", compressor.getId());
        assertEquals("compressor=zlib/level=1", compressor.toString());
    }

    @Test
    public void create_zlib_compresson_level_invalid() {
        int invalid = -1;
        try {
            CompressorFactory.create("zlib", TestUtils.createMap("level", invalid));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertEquals("Invalid compression level: -1", expected.getMessage());
        }

        invalid = 10;
        try {
            CompressorFactory.create("zlib", TestUtils.createMap("level", invalid));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertEquals("Invalid compression level: 10", expected.getMessage());
        }
    }

    @Test
    public void create_compressor_not_supported() {
        final String id = "kkkkkkk";
        try {
            CompressorFactory.create(id, TestUtils.createMap("level", 1));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertEquals("Compressor id:'kkkkkkk' not supported.", expected.getMessage());
        }
    }
}