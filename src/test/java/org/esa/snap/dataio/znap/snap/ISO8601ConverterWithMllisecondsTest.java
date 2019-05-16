package org.esa.snap.dataio.znap.snap;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.esa.snap.core.datamodel.ProductData;
import org.esa.snap.dataio.znap.snap.ISO8601ConverterWithMlliseconds;
import org.junit.*;

import java.text.ParseException;
import java.util.Calendar;

public class ISO8601ConverterWithMllisecondsTest {

    private static final int YEAR = 2023;
    private static final int MONTH = 11;
    private static final int DAY = 13;
    private static final int HOUR = 14;
    private static final int MINUTE = 25;
    private static final int SECOND = 36;
    private static final int MILLISECOND = 126;
    private static final String DATE_TIME_STRING = "" + YEAR + "-" + MONTH + "-" + DAY + "T" + HOUR + ":" + MINUTE + ":" + SECOND + "." + MILLISECOND;

    @Before
    public void setUp() throws Exception {
        assertThat(DATE_TIME_STRING, is(equalTo("2023-11-13T14:25:36.126")));
    }

    @Test
    public void parse() throws ParseException {
        //execution
        final ProductData.UTC parsedUTC = ISO8601ConverterWithMlliseconds.parse(DATE_TIME_STRING);

        assertThat(parsedUTC.getAsCalendar().get(Calendar.YEAR), is(equalTo(YEAR)));
        assertThat(parsedUTC.getAsCalendar().get(Calendar.MONTH) + 1, is(equalTo(MONTH)));
        assertThat(parsedUTC.getAsCalendar().get(Calendar.DAY_OF_MONTH), is(equalTo(DAY)));
        assertThat(parsedUTC.getAsCalendar().get(Calendar.HOUR_OF_DAY), is(equalTo(HOUR)));
        assertThat(parsedUTC.getAsCalendar().get(Calendar.MINUTE), is(equalTo(MINUTE)));
        assertThat(parsedUTC.getAsCalendar().get(Calendar.SECOND), is(equalTo(SECOND)));
        assertThat(parsedUTC.getAsCalendar().get(Calendar.MILLISECOND), is(equalTo(MILLISECOND)));
    }

    @Test
    public void format() throws ParseException {
        //preparation
        final ProductData.UTC parsedUTC = ISO8601ConverterWithMlliseconds.parse(DATE_TIME_STRING);

        //execution
        final String formatedUTC = ISO8601ConverterWithMlliseconds.format(parsedUTC);

        assertThat(formatedUTC, is(equalTo(DATE_TIME_STRING)));
    }
}