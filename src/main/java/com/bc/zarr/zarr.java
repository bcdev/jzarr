package com.bc.zarr;

public class zarr {

    public static OpenBuilder open() {
        return new OpenBuilder();
    }

    private static class OpenBuilder {
        private String path;
        private String mode;
        private Storage storage;

        private OpenBuilder() {
            this.path = path;
            this.mode = mode;
            this.storage = storage;
        }

        public OpenBuilder withPath(String path) {
            this.path = path;
            return this;
        }

        public OpenBuilder withMode(String mode) {
            this.mode = mode;
            return this;
        }

        public OpenBuilder withStorage(Storage storage) {
            this.storage = storage;
            return this;
        }

        public Zarr get() {
            return this.create();
        }

        private Zarr create() {
            return null;
        }
    }

    public static class Storage {

    }

    public interface Zarr {

        boolean isGroup();
        zGroup getAsGroup();
        zArray getAsArray();
    }

    public static class zGroup implements Zarr {

        @Override
        public boolean isGroup() {
            return true;
        }

        @Override
        public zGroup getAsGroup() {
            return this;
        }

        @Override
        public zArray getAsArray() {
            throw new IllegalStateException("This is not an zArray instance. Therefor getAsArray() can not be used.");
        }
    }

    public static class zArray implements Zarr {

        @Override
        public boolean isGroup() {
            return false;
        }

        @Override
        public zGroup getAsGroup() {
            throw new IllegalStateException("This is not a zGroup instance. Therefor getAsGroup() can not be used.");
        }

        @Override
        public zArray getAsArray() {
            return this;
        }
    }
}
