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

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
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

    public static boolean deleteLineContaining(String str, InputStream is, OutputStream os) throws IOException {
        boolean deleted = false;
        final List<String> lines;
        try (LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(is))) {
            lines = lineNumberReader.lines().collect(Collectors.toList());
        }
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(os))) {
            for (String line : lines) {
                if (line.contains(str)) {
                    deleted = true;
                    continue;
                }
                pw.println(line);
            }
        }
        return deleted;
    }
}
