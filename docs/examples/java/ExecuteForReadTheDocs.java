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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * This should be rerun after any code refactoring or changes in example code
 * to automatically generate needed up to date output files referenced by some
 * sphinx *.rst (reStructuredText) files.
 */
public class ExecuteForReadTheDocs {
    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final Properties properties = new Properties();
        properties.load(Files.newBufferedReader(Paths.get("maven.properties")));
        System.setProperty("jna.library.path", properties.getProperty("bloscJnaLibraryPath"));

        Path workingDir = Paths.get(".");

        deleteContentOfDocsExamplesOutputDirectory(workingDir);

        executeAll_ReadTheDocs_JavaClasses(args, workingDir);
    }

    private static void executeAll_ReadTheDocs_JavaClasses(String[] args, Path workingDir) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        List<Path> javaFilePaths = Files.walk(workingDir)
                .filter(path -> {
                    String sPath = path.getFileName().toString();
                    return sPath.endsWith("_rtd.java");
                })
                .collect(Collectors.toList());


        for (Path path : javaFilePaths) {
            System.out.println("path = " + path);
            String className = path.getFileName().toString();
            className = className.substring(0, className.lastIndexOf("."));
            Class<?> aClass = findClass(className);
            Method mainMethod = aClass.getMethod("main", String[].class);
            mainMethod.invoke(null, new Object[]{args});
        }
    }

    private static void deleteContentOfDocsExamplesOutputDirectory(Path workingDir) throws IOException {
        Path outputDir = Files.walk(workingDir)
                .filter(path -> {
                    String sPath = path.toString();
                    if (!sPath.endsWith("output")) return false;
                    return sPath.matches(".*[\\\\/]docs[\\\\/]examples[\\\\/]output");
                })
                .collect(Collectors.toList())
                .get(0);
        deleteFilesAndDirectoriesInReverseOrder(outputDir);
    }

    private static void deleteFilesAndDirectoriesInReverseOrder(Path startDir) throws IOException {
        String startName = startDir.getFileName().toString();
        Files.walk(startDir)
                .filter(path -> !path.toString().endsWith(startName))
                .collect(Collectors.toCollection(LinkedList::new))
                .descendingIterator()
                .forEachRemaining(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                );
    }

    private static Class<?> findClass(String className) throws ClassNotFoundException {
        return ExecuteForReadTheDocs.class.getClassLoader().loadClass(className);
    }
}

