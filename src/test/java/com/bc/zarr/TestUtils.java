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

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.bc.zarr.ZarrConstants.*;
import static org.junit.Assert.*;

public class TestUtils {

    public static Object getPrivateFieldObject(Object obj, String name) throws NoSuchFieldException, IllegalAccessException {
        Field field = null;
        try {
            field = obj.getClass().getDeclaredField(name);
        } catch (NoSuchFieldException | SecurityException e) {
            //e.printStackTrace();
        }
        if (field == null) {
            field = obj.getClass().getSuperclass().getDeclaredField(name);
        }
        field.setAccessible(true);
        return field.get(obj);
    }

    public static Map<String, Object> createMap(Object... args) {
        final HashMap<String, Object> map = new HashMap<>();
        assertEquals(args.length % 2, 0);
        for (int i = 0; i < args.length; i += 2) {
            String key = (String) args[i];
            Object val = args[i + 1];
            map.put(key, val);
        }
        return map;
    }

    public static String getZgroupContent(Path dir) throws IOException {
        return readContent(dir.resolve(FILENAME_DOT_ZGROUP));
    }

    public static String getZattrsContent(Path dir) throws IOException {
        return readContent(dir.resolve(FILENAME_DOT_ZATTRS));
    }

    public static String getZarrayContent(Path dir) throws IOException {
        return readContent(dir.resolve(FILENAME_DOT_ZARRAY));
    }

    public static String readContent(Path path) throws IOException {
        return Files.lines(path).collect(Collectors.joining());
    }
}
