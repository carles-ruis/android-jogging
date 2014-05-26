package com.carles.jogging.util;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.text.DateFormat;

/**
 * Created by carles1 on 20/05/14.
 */
public class FormatUtil {

    private static final DateTimeFormatter time = DateTimeFormat.forPattern("HH:mm:ss");
    private static final DateTimeFormatter date = DateTimeFormat.forPattern("dd:MM:yyyy");
    private static final DateTimeFormatter datetime = DateTimeFormat.forPattern("dd:MM:yyyy HH:mm:ss");

    public static String time(long millis) {
        return time.print(millis);
    }

    public static String date(long millis) {
        return date.print(millis);
    }

    public static String datetime(long millis) {
        return datetime.print(millis);
    }

}
