package com.carles.jogging.helper;

import android.content.Context;

import com.carles.jogging.C;
import com.carles.jogging.R;
import com.carles.jogging.exception.JoggingAppError;

/**
 * Created by carles1 on 20/04/14.
 */
public class ConversionHelper {

    public static Integer textDistanceToMeters(Context context, String src) {
        Integer ret;
        String errorMsg = "Trying to convert" + src + " into kilometers";

        if (context.getString(R.string.half_marathon).equals(src)) {
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

}
