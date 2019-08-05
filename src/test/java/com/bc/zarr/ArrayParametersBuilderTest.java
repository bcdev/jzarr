package com.bc.zarr;

import org.junit.Test;

import java.nio.ByteOrder;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class ArrayParametersBuilderTest {

    @Test
    public void buildWithAllMethodCalls() {
        final ArrayParameters parameters = ArrayParameters.builder()
                .withShape(800, 1000)
                .withChunks(200, 100)
                .withDataType(ZarrDataType.i2)
                .withByteOrder(ByteOrder.LITTLE_ENDIAN)
                .withFillValue(42)
                .withCompressor(CompressorFactory.nullCompressor)
                .build();

        assertThat(parameters.getShape(), is(new int[]{800, 1000}));
        assertThat(parameters.getChunks(), is(new int[]{200, 100}));
        assertThat(parameters.getDataType(), is(ZarrDataType.i2));
        assertThat(parameters.getByteOrder(), is(ByteOrder.LITTLE_ENDIAN));
        assertThat(parameters.getFillValue(), is(42));
        assertThat(parameters.getCompressor(), is(sameInstance(CompressorFactory.nullCompressor)));
    }

    @Test
    public void ShapeMustBeGiven_noCallToWithShape() {
        try {
            //execution
            ArrayParameters.builder()
                    .withChunks(2, 3, 4)
                    .withByteOrder(ByteOrder.LITTLE_ENDIAN)
                    .withDataType(ZarrDataType.i2)
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
            ArrayParameters.builder()
                    .withChunks(2, 3, 4)
                    .withByteOrder(ByteOrder.LITTLE_ENDIAN)
                    .withDataType(ZarrDataType.i2)
                    .withFillValue(23)
                    .withCompressor(null)
                    .withShape() // no values given
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
            ArrayParameters.builder()
                    .withShape(1000, 1000)
                    .withChunks(10, 10, 100)
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
        final ArrayParameters parameters = ArrayParameters.builder()
                .withShape(1000, 1000)
                .withChunked(false)
                .build();

        //verification
        assertThat(parameters.getChunks(), is(equalTo(new int[]{1000, 1000})));
    }

    @Test
    public void DefaultValues() {
        //execution
        final ArrayParameters parameters = ArrayParameters.builder()
                .withShape(3800, 5000)
                .build();

        //verification
        assertThat(parameters.getShape(), is(new int[]{3800, 5000}));
        assertThat(parameters.getChunks(), is(new int[]{476, 501}));
        assertThat(parameters.isChunked(), is(true));
        assertThat(parameters.getDataType(), is(ZarrDataType.f8));
        assertThat(parameters.getByteOrder(), is(ByteOrder.BIG_ENDIAN));
        assertThat(parameters.getFillValue(), is(0));
        assertThat(parameters.getCompressor().getId(), is("zlib"));
        assertThat(parameters.getCompressor().getLevel(), is(1));
    }

    @Test
    public void Rebuild() {
        //preparation
        final ArrayParameters parameters = ArrayParameters.builder()
                .withShape(3800, 5000)
                .withChunks(12, 13)
                .withDataType(ZarrDataType.i2)
                .withByteOrder(ByteOrder.LITTLE_ENDIAN)
                .withFillValue(null)
                .withCompressor(CompressorFactory.nullCompressor)
                .build();

        //execution
        final ArrayParameters newParams = parameters.toBuilder()
                .withShape(123, 456)
                .build();

        //verification
        assertThat(newParams.getShape(), is(new int[]{123, 456}));
        assertThat(newParams.getChunks(), is(new int[]{12, 13}));
        assertThat(newParams.getDataType(), is(ZarrDataType.i2));
        assertThat(newParams.getByteOrder(), is(ByteOrder.LITTLE_ENDIAN));
        assertThat(newParams.getFillValue(), is(nullValue()));
        assertThat(newParams.getCompressor(), is(sameInstance(CompressorFactory.nullCompressor)));
    }
}