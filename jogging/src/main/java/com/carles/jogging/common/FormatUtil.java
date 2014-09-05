package com.carles.jogging.common;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.text.DateFormat;
import java.util.concurrent.TimeUnit;

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


    /**
     * Formats a timestamp to a user readable format containing the time in hours, minutes, and seconds
     */
    public static String runningTime(long millis) {
        String ret = "";

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        if (hours == 0l) {
            ret = String.format("  %02d:%02d", minutes, seconds);
        } else {
            ret = String.format("%1d:%02d:%02d", hours, minutes, seconds);
        }

        return ret;
    }

}
