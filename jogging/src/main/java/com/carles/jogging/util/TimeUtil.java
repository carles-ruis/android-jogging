package com.carles.jogging.util;

/**
 * Created by carles1 on 23/04/14.
 */
public class TimeUtil {

    public static void stop(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            Log.i("Interrupted exception during countdown. Will be ignored");
        }
    }
}
