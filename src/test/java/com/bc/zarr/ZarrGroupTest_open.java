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

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Collections;

import static com.bc.zarr.ZarrConstants.FILENAME_DOT_ZGROUP;
import static com.bc.zarr.ZarrUtils.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class ZarrGroupTest_open {

    private Path groupPath;
    private Path dotZGroupPath;

    @Before
    public void setUp() throws Exception {
        final FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
        groupPath = fs.getPath("/test").resolve("group");
        Files.createDirectories(groupPath);
        dotZGroupPath = groupPath.resolve(FILENAME_DOT_ZGROUP);
        try (final BufferedWriter writer = Files.newBufferedWriter(dotZGroupPath)) {
            toJson(Collections.singletonMap("zarr_format", 2), writer);
        }
    }

    @Test
    public void open_allIsAsExpected() throws IOException {
        //preparation --> setUp
        //execution
        final ZarrGroup zGroup = ZarrGroup.open(groupPath);
        //verification
        assertThat(zGroup, is(notNullValue()));
    }

    @Test
    public void open_pathIsNull_willBeDelegatedToCreateGroupWithInMemoryStore() throws IOException {
        //preparation
        final Path groupPath = null;
        //execution
        final ZarrGroup open = ZarrGroup.open(groupPath);
        //verification
        assertThat(open, is(notNullValue()));
    }

    @Test
    public void open_notExistingDirectory() {
        //preparation
        final Path notExistingDirPath = groupPath.getParent().resolve("notExistingDirPath");
        try {
            //execution
            ZarrGroup.open(notExistingDirPath);
            fail("IOException expected");
        } catch (IOException expected) {
            //verification
            assertThat(expected.getMessage(), is("Path '/test/notExistingDirPath' is not a valid path or not a directory."));
        }
    }

    @Test
    public void open_dotZGroup_fileDoesNotExist() throws IOException {
        //preparation
        Files.delete(dotZGroupPath);
        try {
            //execution
            ZarrGroup.open(groupPath);
            fail("IOException expected");
        } catch (IOException expected) {
            //verification
            assertThat(expected.getMessage(), is("'.zgroup' expected but is not readable or missing in store."));
        }
    }

    @Test
    public void open_dotZGroup_isNotZarrFormat2() throws IOException {
        //preparation
        try (BufferedWriter writer = Files.newBufferedWriter(dotZGroupPath)) {
            toJson(Collections.singletonMap("zarr_format", 1.3), writer);
        }
        try {
            //execution
            ZarrGroup.open(groupPath);
            fail("IOException expected");
        } catch (IOException expected) {
            //verification
            assertThat(expected.getMessage(), is("Zarr format 2 expected but is '1.3'"));
        }
    }
}