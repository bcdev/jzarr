package com.bc.zarr;

import org.junit.Test;

import java.nio.ByteOrder;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class ArrayParametersBuilderTest {

    @Test
    public void buildWithAllMethodCalls() {
        final ArrayParams.Params parameters = new ArrayParams()
                .shape(800, 1000)
                .chunks(200, 100)
                .dataType(DataType.i2)
                .withByteOrder(ByteOrder.LITTLE_ENDIAN)
                .withFillValue(42)
                .withCompressor(CompressorFactory.nullCompressor)
                .build();

        assertThat(parameters.getShape(), is(new int[]{800, 1000}));
        assertThat(parameters.getChunks(), is(new int[]{200, 100}));
        assertThat(parameters.getDataType(), is(DataType.i2));
        assertThat(parameters.getByteOrder(), is(ByteOrder.LITTLE_ENDIAN));
        assertThat(parameters.getFillValue(), is(42));
        assertThat(parameters.getCompressor(), is(sameInstance(CompressorFactory.nullCompressor)));
    }

    @Test
    public void ShapeMustBeGiven_noCallToWithShape() {
        try {
            //execution
            new ArrayParams()
                    .chunks(2, 3, 4)
                    .withByteOrder(ByteOrder.LITTLE_ENDIAN)
                    .dataType(DataType.i2)
                    .withFillValue(23)
                    .withCompressor(null)
                    // .withShape(3,4,5) // suspended
                    .build();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            //verification
            assertThat(expected.getMessage(), is("Shape must be given."));
        }
    }

    @Test
    public void ShapeMustBeGiven_bulderMethodCallWithZeroArguments() {
        try {
            //execution
            new ArrayParams()
                    .chunks(2, 3, 4)
                    .withByteOrder(ByteOrder.LITTLE_ENDIAN)
                    .dataType(DataType.i2)
                    .withFillValue(23)
                    .withCompressor(null)
                    .shape() // no values given
                    .build();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            //verification
            assertThat(expected.getMessage(), is("Shape must be given."));
        }
    }

    @Test
    public void WrongNumberOfChunkDimensions() {
        try {
            //execution
            new ArrayParams()
                    .shape(1000, 1000)
                    .chunks(10, 10, 100)
                    .build();
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException expected) {
            //verification
            assertThat(expected.getMessage(), is("Chunks must have the same number of dimensions as shape. Expected: 2 but was 3 !"));
        }
    }

    @Test
    public void Unchuncked() {
        //execution
        final ArrayParams.Params parameters = new ArrayParams()
                .shape(1000, 1000)
                .withChunked(false)
                .build();

        //verification
        assertThat(parameters.getChunks(), is(equalTo(new int[]{1000, 1000})));
    }

    @Test
    public void DefaultValues() {
        //execution
        final ArrayParams.Params parameters = new ArrayParams()
                .shape(3800, 5000)
                .build();

        //verification
        assertThat(parameters.getShape(), is(new int[]{3800, 5000}));
        assertThat(parameters.getChunks(), is(new int[]{475, 500}));
        assertThat(parameters.isChunked(), is(true));
        assertThat(parameters.getDataType(), is(DataType.f8));
        assertThat(parameters.getByteOrder(), is(ByteOrder.BIG_ENDIAN));
        assertThat(parameters.getFillValue(), is(0));
        assertThat(parameters.getCompressor().getId(), is("zlib"));
        assertThat(parameters.getCompressor().getLevel(), is(1));
    }

    @Test
    public void Rebuild() {
        //preparation
        final ArrayParams.Params parameters = new ArrayParams()
                .shape(3800, 5000)
                .chunks(12, 13)
                .dataType(DataType.i2)
                .withByteOrder(ByteOrder.LITTLE_ENDIAN)
                .withFillValue(null)
                .withCompressor(CompressorFactory.nullCompressor)
                .build();

        //execution
        final ArrayParams.Params newParams = parameters.toBuilder()
                .shape(123, 456)
                .build();

        //verification
        assertThat(newParams.getShape(), is(new int[]{123, 456}));
        assertThat(newParams.getChunks(), is(new int[]{12, 13}));
        assertThat(newParams.getDataType(), is(DataType.i2));
        assertThat(newParams.getByteOrder(), is(ByteOrder.LITTLE_ENDIAN));
        assertThat(newParams.getFillValue(), is(nullValue()));
        assertThat(newParams.getCompressor(), is(sameInstance(CompressorFactory.nullCompressor)));
    }
}