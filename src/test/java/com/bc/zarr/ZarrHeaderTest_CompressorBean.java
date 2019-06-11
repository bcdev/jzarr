package com.bc.zarr;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import com.bc.zarr.chunk.Compressor;
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
        final ZarrHeader zarrHeader = new ZarrHeader(new int[]{1001, 1002}, new int[]{101, 102}, ZarrDataType.f4.name(), 4.2, Compressor.zlib_L1);

        //verification
        assertThat(zarrHeader.getCompressor(), is(equalTo(compressorBean)));
    }

}