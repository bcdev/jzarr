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

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

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
        assertNull(compressor.getId());
        assertNull(compressor.toString());
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

    @Test
    public void createBloscValidCnames() {
        String[] cnames = { "zstd", "blosclz", "lz4", "lz4hc", "zlib" };
        for (int i = 0; i < cnames.length; i += 1) {
            final Compressor compressor = CompressorFactory.create("blosc", "cname", cnames[i]);
            assertNotNull(compressor);
            assertEquals("blosc", compressor.getId());
            assertEquals(
                "compressor=blosc/cname=" + cnames[i] +
                "/clevel=5/blocksize=0/shuffle=1", compressor.toString());
        }
    }

    @Test
    public void createBloscInvalidCname() {
        try {
            CompressorFactory.create("blosc", "cname", "unsupported");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            assertEquals("blosc: compressor not supported: 'unsupported'; expected one of [zstd, blosclz, lz4, lz4hc, zlib]", expected.getMessage());
        }
   }

   @Test
   public void createBloscValidClevel() {
       final Compressor compressor = CompressorFactory.create("blosc", "clevel", 1);
       assertNotNull(compressor);
       assertEquals("blosc", compressor.getId());
       assertEquals(
           "compressor=blosc/cname=lz4" +
           "/clevel=1/blocksize=0/shuffle=1", compressor.toString());
   }

   @Test
   public void createBloscInvalidClevel() {
       try {
           CompressorFactory.create("blosc", "clevel", -1);
           fail("IllegalArgumentException expected");
       } catch (IllegalArgumentException expected) {
           assertEquals("blosc: clevel parameter must be between 0 and 9 but was: -1", expected.getMessage());
       }

       try {
           CompressorFactory.create("blosc", "clevel", 10);
           fail("IllegalArgumentException expected");
       } catch (IllegalArgumentException expected) {
           assertEquals(
               "blosc: clevel parameter must be between 0 and 9 but was: 10",
               expected.getMessage());
       }
   }

   @Test
   public void createBloscValidShuffles() {
       int[] shuffles = { 0, 1, 2 };
       for (int i = 0; i < shuffles.length; i += 1) {
           final Compressor compressor = CompressorFactory.create("blosc", "shuffle", shuffles[i]);
           assertNotNull(compressor);
           assertEquals("blosc", compressor.getId());
           assertEquals(
               "compressor=blosc/cname=lz4" +
               "/clevel=5/blocksize=0/shuffle=" +
               shuffles[i], compressor.toString());
       }
   }

   @Test
   public void createBloscInvalidShuffle() {
       try {
           CompressorFactory.create("blosc", "shuffle", -1);
           fail("IllegalArgumentException expected");
       } catch (IllegalArgumentException expected) {
           assertEquals(
               "blosc: shuffle type not supported: '-1'; expected one of [0 (NOSHUFFLE), 1 (BYTESHUFFLE), 2 (BITSHUFFLE)]",
               expected.getMessage());
       }
  }

  @Test
  public void createBloscValidBlockSizes() {
      int[] blockSizes = { 0, 1, 20 };
      for (int i = 0; i < blockSizes.length; i += 1) {
          final Compressor compressor = CompressorFactory.create("blosc", "blocksize", blockSizes[i]);
          assertNotNull(compressor);
          assertEquals("blosc", compressor.getId());
          assertEquals(
              "compressor=blosc/cname=lz4" +
              "/clevel=5/blocksize=" + blockSizes[i] +
              "/shuffle=1", compressor.toString());
      }
  }
}
