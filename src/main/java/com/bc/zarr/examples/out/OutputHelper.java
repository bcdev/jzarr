package com.bc.zarr.examples.out;

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

}
