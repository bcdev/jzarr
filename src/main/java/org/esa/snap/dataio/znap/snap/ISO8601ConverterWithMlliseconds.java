package org.esa.snap.dataio.znap.snap;

import org.esa.snap.core.datamodel.ProductData;

import java.text.DateFormat;
import java.text.ParseException;

public class ISO8601ConverterWithMlliseconds {

    private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private static final DateFormat FORMAT;
    private static final String SECONDS_PATTERN = PATTERN.substring(0, PATTERN.lastIndexOf("."));
    private static final DateFormat SECONDS_FORMAT;

    static {
        FORMAT = ProductData.UTC.createDateFormat(PATTERN);
        SECONDS_FORMAT = ProductData.UTC.createDateFormat(SECONDS_PATTERN);
    }

    public static ProductData.UTC parse(String iso8601String) throws ParseException {
        return ProductData.UTC.parse(iso8601String, SECONDS_FORMAT);
    }

    public static String format(ProductData.UTC utc) {
        return FORMAT.format(utc.getAsDate());
    }
}
