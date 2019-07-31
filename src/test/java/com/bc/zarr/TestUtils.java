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
