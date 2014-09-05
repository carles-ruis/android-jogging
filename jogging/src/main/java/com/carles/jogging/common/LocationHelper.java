package com.carles.jogging.common;

import android.content.Context;
import android.location.Location;

import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.JoggingAppError;

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

        // TODO delete. 150 meters for TEST
        if ("TEST".equals(src)) {
            return 150;
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
        StringBuilder sb = new StringBuilder().append(lat).append(" ---- ").append(lon).append(" ---- ").append(acc).append(" ---- ").append(t).append(" --- ");
        return sb.toString();
    }

}
