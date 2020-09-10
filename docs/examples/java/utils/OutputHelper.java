package utils;

import com.bc.zarr.JZarrException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

public class OutputHelper {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    public static void printHeadline() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        final String methodName = stackTrace[2].getMethodName();
        final String[] strings = splitCamelCase(methodName);
        final String headline = String.join(" ", strings);
        final int n = headline.trim().length();

        System.out.println();
        System.out.print(ANSI_CYAN_BACKGROUND);
        System.out.print(ANSI_BLACK);
        System.out.println(headline);
        System.out.println(String.join("", Collections.nCopies(n, "=")));
        System.out.println(ANSI_RESET); // reset
    }

    public static void printMethodSeparator() {
        System.out.println();
        System.out.println("------------------------------------------------------------------");
        System.out.println();
    }

    private static String[] splitCamelCase(String string) {
        return string.split("(?<!^)(?=[A-Z])");
    }

    public static void createOutput(Writer writer) throws IOException, JZarrException {
        final String fileName = createOutputFilename();
        String methodName = getOutputMethodName();
        final Path workDir = Paths.get(".");
        final Path outDir = workDir.resolve("docs").resolve("examples").resolve("output");
        Files.createDirectories(outDir);
        final Path filePath = outDir.resolve(fileName);
        if (!Files.exists(filePath)) {
            OutputStream stream = Files.newOutputStream(filePath);
            stream.close();
        }
        try (OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.APPEND);
             PrintStream printStream = new PrintStream(outputStream)) {
            printStream.println(methodName + "_output_start");
            writer.write(printStream);
            printStream.println(methodName + "__output_end__");
            printStream.println();
            printStream.println("=====================================================================================");
            printStream.println();
        }
    }

    private static String createOutputFilename() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        final StackTraceElement element = stackTrace[3];
        String className = element.getClassName();
        if (className.contains(".")) {
            className = className.substring(className.lastIndexOf(".") + 1);
        }
        return className + ".txt";
    }

    private static String getOutputMethodName() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        final StackTraceElement element = stackTrace[3];
        return element.getMethodName();
    }

    public interface Writer {
        void write(PrintStream out) throws IOException, JZarrException;
    }
}
