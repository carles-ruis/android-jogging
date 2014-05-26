package com.carles.jogging.util;

/**
 * Created by carles1 on 26/04/14.
 */
public class Decimals {

    /**
     * Concatenes a string with a number formatted to one decimal
     */
    public static String one(String s, float f) {
        return String.format(s + "%.1f", f);
    }

    /**
     * Parses a float to a String keeping one decimal
     */
    public static String one(float f) {
        return String.format("%.1f", f);
    }

    /**
     * Concatenes a string with a number formatted to n decimals
     */
    public static String n(String s, double f, int n) {
        return String.format(s + "%."+n+"f", f);
    }

}
