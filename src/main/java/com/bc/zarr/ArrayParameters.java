package com.bc.zarr;

import java.nio.ByteOrder;
import java.util.Arrays;

public class ArrayParameters {
    private final int[] shape;
    private final int[] chunks;
    private final ZarrDataType dataType;
    private final ByteOrder byteOrder;
    private final Number fillValue;
    private final Compressor compressor;

    private ArrayParameters(int[] shape, int[] chunks, ZarrDataType dataType, ByteOrder byteOrder, Number fillValue, Compressor compressor) {
        this.shape = shape;
        this.chunks = chunks;
        this.dataType = dataType;
        this.byteOrder = byteOrder;
        this.fillValue = fillValue;
        this.compressor = compressor;
    }

    public int[] getShape() {
        return shape;
    }

    public int[] getChunks() {
        return chunks;
    }

    public boolean isChunked() {
        return !Arrays.equals(shape, chunks);
    }

    public ZarrDataType getDataType() {
        return dataType;
    }

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public Number getFillValue() {
        return fillValue;
    }

    public Compressor getCompressor() {
        return compressor;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        Builder builder = new Builder();
        builder.shape = getShape();
        builder.chunks = getChunks();
        builder.chunked = isChunked();
        builder.dataType = getDataType();
        builder.byteOrder = getByteOrder();
        builder.fillValue = getFillValue();
        builder.compressor = getCompressor();
        return builder;
    }

    /**
     * {@code ArrayParameters} builder static inner class.
     */
    public static final class Builder {
        private int[] shape;
        private int[] chunks;
        private boolean chunked = true;
        private ZarrDataType dataType = ZarrDataType.f8;
        private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
        private Number fillValue = 0;
        private Compressor compressor = CompressorFactory.create("zlib", 1);

        private Builder() {
        }

        /**
         * Sets the {@code shape} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param shape the {@code shape} to set
         * @return a reference to this Builder
         */
        public Builder withShape(int... shape) {
            this.shape = shape;
            return this;
        }

        /**
         * Sets the {@code chunks} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param chunks the {@code chunks} to set
         * @return a reference to this Builder
         */
        public Builder withChunks(int... chunks) {
            this.chunks = chunks;
            return this;
        }

        /**
         * Sets the {@code chunked} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param chunked the {@code chunked} to set
         * @return a reference to this Builder
         */
        public Builder withChunked(boolean chunked) {
            this.chunked = chunked;
            return this;
        }

        /**
         * Sets the {@code dataType} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param dataType the {@code dataType} to set
         * @return a reference to this Builder
         */
        public Builder withDataType(ZarrDataType dataType) {
            this.dataType = dataType;
            return this;
        }

        /**
         * Sets the {@code byteOrder} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param byteOrder the {@code byteOrder} to set
         * @return a reference to this Builder
         */
        public Builder withByteOrder(ByteOrder byteOrder) {
            this.byteOrder = byteOrder;
            return this;
        }

        /**
         * Sets the {@code fillValue} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param fillValue the {@code fillValue} to set
         * @return a reference to this Builder
         */
        public Builder withFillValue(Number fillValue) {
            this.fillValue = fillValue;
            return this;
        }

        /**
         * Sets the {@code compressor} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param compressor the {@code compressor} to set
         * @return a reference to this Builder
         */
        public Builder withCompressor(Compressor compressor) {
            this.compressor = compressor;
            return this;
        }

        /**
         * Returns a {@code ArrayParameters} built from the parameters previously set.
         *
         * @return a {@code ArrayParameters} built with parameters of this {@code ArrayParameters.Builder}
         */
        public ArrayParameters build() {
            if (shape == null || shape.length == 0) {
                throw new IllegalArgumentException("Shape must be given.");
            }
            if (chunks == null) {
                if (chunked) {
                    chunks = new int[shape.length];
                    for (int i = 0; i < shape.length; i++) {
                        int shapeDim = shape[i];
                        chunks[i] = shapeDim;
                        final int numChunks = (shapeDim / 512) + 1;
                        if (numChunks > 1) {
                            chunks[i] = (shapeDim / numChunks) + 1;
                        }
                    }
                } else {
                    chunks = Arrays.copyOf(shape, shape.length);
                }
            }

            if (shape.length != chunks.length) {
                throw new IllegalArgumentException(
                        "Chunks must have the same number of dimensions as shape. " +
                                "Expected: " + shape.length + " but was " + chunks.length + " !");
            }
            return new ArrayParameters(shape, chunks, dataType, byteOrder, fillValue, compressor);
        }
    }
}
