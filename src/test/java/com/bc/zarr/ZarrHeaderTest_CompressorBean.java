package com.bc.zarr;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.*;

public class ZarrHeaderTest_CompressorBean {

    private ZarrHeader.CompressorBean compressorBean;

    @Before
    public void setUp() throws Exception {
        compressorBean = new ZarrHeader.CompressorBean("zlib", 1);
    }

    @Test
    public void getters() {
        assertThat(compressorBean.getId(), is("zlib"));
        assertThat(compressorBean.getLevel(), is(1));
    }

    @Test
    public void equals() {
        assertThat(compressorBean.equals(compressorBean), is(true));
        assertThat(compressorBean.equals(null), is(false));
        assertThat(compressorBean.equals(new ZarrHeader.CompressorBean("zlib", 2)), is(false));
        assertThat(compressorBean.equals(new ZarrHeader.CompressorBean("zzzz", 1)), is(false));
        assertThat(compressorBean.equals(new ZarrHeader.CompressorBean("zlib", 1)), is(true));
    }

    @Test
    public void createdCompressorBean() {
        //execution
        final Compressor compressor = CompressorFactory.create("zlib", 1);
        final ZarrHeader zarrHeader = new ZarrHeader(new int[]{1001, 1002}, new int[]{101, 102}, DataType.f4.name(),null, 4.2, compressor);

        //verification
        assertThat(zarrHeader.getCompressor(), is(equalTo(compressorBean)));
    }

    @Test
    public void createJsonFromCompressorBean() {
        final Compressor nullCompressor = CompressorFactory.create("null", 1);
        final ZarrHeader nullZarrHeader = new ZarrHeader(new int[]{1001, 1002}, new int[]{101, 102}, DataType.f4.name(),null, 4.2, nullCompressor);

        String nullJson = ZarrUtils.toJson(nullZarrHeader);
        String expectedNullJson = "{\"chunks\":[101,102],\"compressor\":null,\"dtype\":\"<f4\",\"fill_value\":4.2,\"filters\":null,\"order\":\"C\",\"shape\":[1001,1002],\"zarr_format\":2}";
        assertThat(nullJson, is(equalTo(expectedNullJson)));

        final Compressor zlibCompressor = CompressorFactory.create("zlib", 1);
        final ZarrHeader zlibZarrHeader = new ZarrHeader(new int[]{1001, 1002}, new int[]{101, 102}, DataType.f4.name(),null, 4.2, zlibCompressor);

        String zlibJson = ZarrUtils.toJson(zlibZarrHeader);
        String expectedZlibJson = "{\"chunks\":[101,102],\"compressor\":{\"id\":\"zlib\",\"level\":1},\"dtype\":\"<f4\",\"fill_value\":4.2,\"filters\":null,\"order\":\"C\",\"shape\":[1001,1002],\"zarr_format\":2}";
        assertThat(zlibJson, is(equalTo(expectedZlibJson)));

        final Compressor bloscCompressor = CompressorFactory.create("blosc", 1);
        final ZarrHeader bloscZarrHeader = new ZarrHeader(new int[]{1001, 1002}, new int[]{101, 102}, DataType.f4.name(), null, Float.NaN, bloscCompressor);

        String bloscJson = ZarrUtils.toJson(bloscZarrHeader);
        String expectedBloscJson = "{\"chunks\":[101,102],\"compressor\":{\"id\":\"blosc\",\"clevel\":1},\"dtype\":\"<f4\",\"fill_value\":NaN,\"filters\":null,\"order\":\"C\",\"shape\":[1001,1002],\"zarr_format\":2}";
        assertThat(bloscJson, is(equalTo(expectedBloscJson)));
    }

}