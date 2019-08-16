import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This should be rerun after any code refactoring or changes in example code
 * to automatically generate needed up to date output files referenced by some
 * sphinx *.rst (reStructuredText) files.
 */
public class ExecuteForReadTheDocs {
    public static void main(String[] args) throws IOException, InvalidRangeException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {

        Path workingDir = Paths.get(".");

        List<Path> paths = Files.walk(workingDir)
                .filter(path -> {
                    String sPath = path.getFileName().toString();
                    return sPath.endsWith("_rtd.java");
                })
                .collect(Collectors.toList());

        for (Path path : paths) {
            System.out.println("path = " + path);
            String className = path.getFileName().toString();
            className = className.substring(0, className.lastIndexOf("."));
            Class<?> aClass = findClass(className);
            Method mainMethod = aClass.getMethod("main", String[].class);
            mainMethod.invoke(null, new Object[]{args});
        }
    }

    private static Class<?> findClass(String className) throws ClassNotFoundException {
        return ExecuteForReadTheDocs.class.getClassLoader().loadClass(className);
    }
}

