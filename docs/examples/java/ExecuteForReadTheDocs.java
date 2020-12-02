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

