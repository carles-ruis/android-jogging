package com.carles.jogging.util;

/**
 * Created by carles1 on 26/04/14.
 */
public class Decimals {

    public static String one(String s, float f) {
        return String.format(s + "%.1f", f);
    }

    public static String one(float f) {
        return String.format("%.1f", f);
    }
}
