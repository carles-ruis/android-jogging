package com.carles.jogging.util;

/**
 * Created by carles1 on 26/04/14.
 */
public class Decimals {

    /**
     * Concatenes a string with a number formatted to one decimal
     */
    public static String one(String s, double d) {
        return String.format(s + "%.1f", d);
    }

    /**
     * Parses a float to a String keeping one decimal
     */
    public static String one(double d) {
        return String.format("%.1f", d);
    }

    /**
     * Concatenes a string with a number formatted to n decimals
     */
    public static String n(String s, double d, int n) {
        return String.format(s + "%."+n+"f", d);
    }

    /**
     * Parses a float to a String keeping n decimals
     */
    public static String n(double d , int n) {
        return String.format("%."+n+"f", d);
    }

}
