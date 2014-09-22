package com.carles.jogging.util;

import android.location.Location;

/**
 * Created by carles1 on 20/04/14.
 */
public class LocationHelper {

    /**
     * Returns a string with the important data of a location
     */
    public static String toString(Location location) {
        return Decimals.n(location.getAccuracy(),0);
    }

}
