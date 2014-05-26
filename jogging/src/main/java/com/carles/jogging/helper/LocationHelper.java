package com.carles.jogging.helper;

import android.content.Context;
import android.location.Location;

import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.exception.JoggingAppError;
import com.carles.jogging.util.Decimals;
import com.carles.jogging.util.FormatUtil;

import org.joda.time.format.DateTimeFormat;

import java.util.concurrent.TimeUnit;

/**
 * Created by carles1 on 20/04/14.
 */
public class LocationHelper {

    public static Integer textDistanceToMeters(Context context, String src) {
        Integer ret;
        String errorMsg = "Trying to convert" + src + " into kilometers";

        if (context.getString(R.string.main_half_marathon).equals(src)) {
            return C.HALF_MARATHON_METERS;
        }

        if (src == null) {
            throw new JoggingAppError(errorMsg);
        }

        try {
            String parsed = src.replaceAll("[^\\d.]", "");
            if (parsed.isEmpty()) {
                throw new JoggingAppError(errorMsg);
            }

            ret = Integer.valueOf(parsed) * 1000;
        } catch (NumberFormatException e) {
            throw new JoggingAppError(errorMsg);
        }
        return ret;
    }

    /**
     * Returns a string with the important data of a location: latitutde and longitude, accuracy and a timestamp formatted to be readable
     */
    public static String toString(Location location) {
        String lat = Decimals.n("LATITUDE=", location.getLatitude(), 6);
        String lon = Decimals.n("LONGITUDE=", location.getLongitude(), 6);
        String acc = Decimals.n("ACCURACY=", location.getAccuracy(), 1);
        String t = "TIMESTAMP=" + FormatUtil.time(location.getTime());

        String rt = "";
        if (location.getExtras() != null && location.getExtras().getString(C.EXTRA_FOOTING_TIME) != null) {
            rt = "FOOTING TIME="+location.getExtras().getString(C.EXTRA_FOOTING_TIME);
        }

        StringBuilder sb = new StringBuilder().append(lat).append(" ---- ").append(lon).append(" ---- ").append(acc).append(" ---- ").append(t).append(" --- ").append(rt);
        return sb.toString();
    }

    /**
     * Formats a timestamp to a user readable format containing the time in hours, minutes, and seconds
     */
    public static String formatRunningTime(long time) {
        String ret = "";

        long hours = TimeUnit.MILLISECONDS.toHours(time);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60;

        if (hours == 0l) {
            ret = String.format("  %02d:%02d", minutes, seconds);
        } else {
            ret = String.format("%1d:%02d:%02d", hours, minutes, seconds);
        }

        return ret;
    }

}
