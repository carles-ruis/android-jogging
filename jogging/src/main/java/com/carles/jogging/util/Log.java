package com.carles.jogging.util;

import com.carles.jogging.C;

/**
 * Created by carles1 on 20/04/14.
 */
public class Log {

    public static int i(String msg) {
        return Log.i(C.LOG_TAG, msg);
    }

    public static int i(String tag, String msg) {
        if (C.DEBUG) {
            return android.util.Log.i(tag, msg);
        } else {
            return 0;
        }
    }

    public static int e(String msg) {
        return Log.e(C.LOG_TAG, msg);
    }

    public static int e(String tag, String msg) {
        if (C.DEBUG) {
            return android.util.Log.e(tag, msg);
        } else {
            return 0;
        }
    }
}