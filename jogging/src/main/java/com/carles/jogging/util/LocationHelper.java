package com.carles.jogging.util;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.JoggingAppError;

/**
 * Created by carles1 on 20/04/14.
 */
public class LocationHelper {

    private static final String TAG = LocationHelper.class.getName();

    /**
     * Returns a string with the important data of a location
     */
    public static String toString(Location location) {
        return Decimals.n(location.getAccuracy(),0);
    }

}
